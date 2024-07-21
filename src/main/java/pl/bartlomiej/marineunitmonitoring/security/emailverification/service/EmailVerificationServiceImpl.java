package pl.bartlomiej.marineunitmonitoring.security.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.AccountAlreadyVerifiedException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.EmailVerificationEntity;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.repository.CustomEmailVerificationEntityRepository;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.repository.MongoEmailVerificationEntityRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private final MongoEmailVerificationEntityRepository mongoEmailVerificationEntityRepository;
    private final CustomEmailVerificationEntityRepository customEmailVerificationEntityRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final long emailTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendEmailVerificationPath;

    public EmailVerificationServiceImpl(
            MongoEmailVerificationEntityRepository mongoEmailVerificationEntityRepository,
            CustomEmailVerificationEntityRepository customEmailVerificationEntityRepository,
            UserService userService,
            EmailService emailService,
            @Value("${project-properties.expiration-times.verification.email-token}") long emailTokenExpirationTime,
            @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
            @Value("${project-properties.app.frontend-integration.endpoint-paths.email-verification}") String frontendEmailVerificationPath) {
        this.mongoEmailVerificationEntityRepository = mongoEmailVerificationEntityRepository;
        this.customEmailVerificationEntityRepository = customEmailVerificationEntityRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.emailTokenExpirationTime = emailTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendEmailVerificationPath = frontendEmailVerificationPath;
    }

    @Override
    public Mono<Void> issueVerificationToken(String uid) {
        log.info("Issuing email verification token.");
        return userService.getUser(uid)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(this::createAndSendVerificationToken);
    }

    @Override
    public Mono<Void> verify(String token) {
        log.info("Verifying email verification token.");
        return mongoEmailVerificationEntityRepository.findById(token)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(emailVerificationEntity -> userService.getUser(emailVerificationEntity.getUid()))
                .flatMap(user -> user.getVerified()
                        ? error(AccountAlreadyVerifiedException::new)
                        : just(user)
                )
                .flatMap(user -> userService.verifyUser(user.getId()))
                .then(mongoEmailVerificationEntityRepository.deleteById(token));
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.email-verification.clearing}")
    public void clearAbandonedVerificationIngredients() {
        log.info("Clearing abandoned email verification ingredients.");
        customEmailVerificationEntityRepository.findExpiredTokens()
                .flatMap(emailVerificationEntity -> {
                    log.info("Deleting an expired token.");
                    return mongoEmailVerificationEntityRepository.delete(emailVerificationEntity)
                            .then(just(emailVerificationEntity));
                }).flatMap(emailVerificationEntity ->
                        userService.getUser(emailVerificationEntity.getUid())
                ).flatMap(user -> {
                    log.info("Checking whether user with an expired verification token has been verified.");
                    if (!user.getVerified()) {
                        log.info("Deleting an unverified user.");
                        return userService.deleteUser(user.getId());
                    }
                    log.info("Verified user, terminating flow.");
                    return Mono.empty();
                })
                .doOnError(error -> log.error("Some error occurred in flow: {}", error.getMessage()))
                .subscribe();
    }

    private Mono<Void> createAndSendVerificationToken(User user) {
        return mongoEmailVerificationEntityRepository.save(new EmailVerificationEntity(user.getId(), this.emailTokenExpirationTime))
                .flatMap(emailVerificationEntity -> {
                    log.info("Sending verification email.");
                    return this.sendVerificationEmail(user.getEmail(), emailVerificationEntity.getId());
                });
    }

    private Mono<Void> sendVerificationEmail(String email, String token) {
        return emailService.sendEmail(
                email,
                "Marine Unit Monitoring app - verification email.",
                this.buildVerificationMessage(this.buildVerificationUrl(token))
        );
    }

    private String buildVerificationMessage(String verificationUrl) {
        return "To verify your email click this link: " + verificationUrl;
    }

    private String buildVerificationUrl(String token) {
        return this.frontendUrl + this.frontendEmailVerificationPath + "/" + token;
    }
}