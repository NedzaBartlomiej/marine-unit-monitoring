package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.just;

public abstract class AbstractVerificationTokenService {

    private static final Logger log = LoggerFactory.getLogger(AbstractVerificationTokenService.class);
    private final EmailService emailService;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;
    private final CustomVerificationTokenRepository customVerificationTokenRepository;
    private final UserService userService;

    protected AbstractVerificationTokenService(EmailService emailService, MongoVerificationTokenRepository mongoVerificationTokenRepository, CustomVerificationTokenRepository customVerificationTokenRepository, UserService userService) {
        this.emailService = emailService;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.customVerificationTokenRepository = customVerificationTokenRepository;
        this.userService = userService;
    }

    protected Mono<VerificationToken> saveVerificationToken(VerificationToken verificationToken) {
        log.info("Saving new {}", verificationToken.getType().toLowerCase());
        return mongoVerificationTokenRepository.save(verificationToken);
    }

    protected Mono<Void> sendVerificationToken(String email, String token, String title) {
        log.info("Sending verification token.");
        return emailService.sendEmail(
                email,
                title,
                this.buildVerificationMessage(this.buildVerificationUrl(token))
        );
    }

    protected abstract String buildVerificationMessage(String verificationUrl);

    protected abstract String buildVerificationUrl(String token);

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.email-verification.clearing}")
    private void clearAbandonedVerificationIngredients() {
        log.info("Clearing abandoned verification tokens.");
        customVerificationTokenRepository.findExpiredTokens()
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
}