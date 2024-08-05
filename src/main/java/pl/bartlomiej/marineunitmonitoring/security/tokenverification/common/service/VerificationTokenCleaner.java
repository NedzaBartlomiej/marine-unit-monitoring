package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@Service
public class VerificationTokenCleaner {

    private static final Logger log = LoggerFactory.getLogger(VerificationTokenCleaner.class);
    private final CustomVerificationTokenRepository customVerificationTokenRepository;
    private final MongoVerificationTokenRepository<VerificationToken> mongoVerificationTokenRepository;
    private final UserService userService;

    public VerificationTokenCleaner(CustomVerificationTokenRepository customVerificationTokenRepository, MongoVerificationTokenRepository<VerificationToken> mongoVerificationTokenRepository, UserService userService) {
        this.customVerificationTokenRepository = customVerificationTokenRepository;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.userService = userService;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.verification-tokens.clearing}")
    public void clearAbandonedVerificationIngredients() {
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
                    return empty();
                })
                .doOnError(error -> log.error("Some error occurred in flow: {}", error.getMessage()))
                .subscribe();
    }
}
