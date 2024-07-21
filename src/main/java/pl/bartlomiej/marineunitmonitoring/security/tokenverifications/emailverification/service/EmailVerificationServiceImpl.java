package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.AccountAlreadyVerifiedException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification.EmailVerificationToken;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private final UserService userService;
    private final EmailService emailService;
    private final long emailTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendEmailVerificationPath;
    private final CustomVerificationTokenRepository customVerificationTokenRepository;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;

    public EmailVerificationServiceImpl(
            UserService userService,
            EmailService emailService,
            @Value("${project-properties.expiration-times.verification.email-token}") long emailTokenExpirationTime,
            @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
            @Value("${project-properties.app.frontend-integration.endpoint-paths.email-verification}") String frontendEmailVerificationPath,
            CustomVerificationTokenRepository customVerificationTokenRepository,
            MongoVerificationTokenRepository mongoVerificationTokenRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.emailTokenExpirationTime = emailTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendEmailVerificationPath = frontendEmailVerificationPath;
        this.customVerificationTokenRepository = customVerificationTokenRepository;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
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
        return mongoVerificationTokenRepository.findById(token)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(verificationToken -> userService.getUser(verificationToken.getUid()))
                .flatMap(user -> user.getVerified()
                        ? error(AccountAlreadyVerifiedException::new)
                        : just(user)
                )
                .flatMap(user -> userService.verifyUser(user.getId()))
                .then(mongoVerificationTokenRepository.deleteById(token));
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.email-verification.clearing}")
    public void clearAbandonedVerificationIngredients() {
        log.info("Clearing abandoned email verification ingredients.");
        customVerificationTokenRepository.findExpiredTokens(VerificationTokenType.EMAIL_VERIFICATION.name())
                .flatMap(verificationToken -> {
                    log.info("Deleting an expired token.");
                    return mongoVerificationTokenRepository.delete(verificationToken)
                            .then(just(verificationToken));
                }).flatMap(verificationToken ->
                        userService.getUser(verificationToken.getUid())
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
        return mongoVerificationTokenRepository.save(
                        new EmailVerificationToken(
                                user.getId(),
                                this.emailTokenExpirationTime,
                                VerificationTokenType.EMAIL_VERIFICATION.name(),
                                null
                        )
                )
                .flatMap(verificationToken -> {
                    log.info("Sending verification email.");
                    return this.sendVerificationEmail(user.getEmail(), verificationToken.getId());
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