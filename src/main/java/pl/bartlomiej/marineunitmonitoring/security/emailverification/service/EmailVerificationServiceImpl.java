package pl.bartlomiej.marineunitmonitoring.security.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.RestControllerGlobalErrorHandler;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.AccountAlreadyVerifiedException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.EmailVerificationEntity;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.repository.CustomEmailVerificationEntityRepository;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.repository.MongoEmailVerificationEntityRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service // todo - test
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(RestControllerGlobalErrorHandler.class);
    private final MongoEmailVerificationEntityRepository mongoEmailVerificationEntityRepository;
    private final CustomEmailVerificationEntityRepository customEmailVerificationEntityRepository;
    private final UserService userService;

    public EmailVerificationServiceImpl(MongoEmailVerificationEntityRepository mongoEmailVerificationEntityRepository, CustomEmailVerificationEntityRepository customEmailVerificationEntityRepository, UserService userService) {
        this.mongoEmailVerificationEntityRepository = mongoEmailVerificationEntityRepository;
        this.customEmailVerificationEntityRepository = customEmailVerificationEntityRepository;
        this.userService = userService;
    }

    @Override
    public Mono<Void> issueVerificationToken(String uid) {
        return userService.getUser(uid)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(user -> mongoEmailVerificationEntityRepository
                        .save(new EmailVerificationEntity(uid)) // todo - sendEmail() -> maybe smth like interface EmailService -> VerificationEmailService etc.
                )
                .then();
    }

    @Override
    public Mono<Void> verify(String token) {
        return mongoEmailVerificationEntityRepository.findById(token)
                .switchIfEmpty(error(AccountAlreadyVerifiedException::new))
                .flatMap(emailVerificationEntity -> userService.getUser(emailVerificationEntity.getUid()))
                .flatMap(user -> userService.verifyUser(user.getId()))
                .then(mongoEmailVerificationEntityRepository.deleteById(token));
    }

    @EventListener(ApplicationReadyEvent.class)
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
}
