package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.InvalidVerificationTokenException;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.EmailService;
import pl.bartlomiej.marineunitmonitoring.emailsending.verificationemail.VerificationEmail;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Function;

import static reactor.core.publisher.Mono.*;

@Service
public abstract class AbstractVerificationTokenService<T extends VerificationToken, CarrierObject, VerifiedActionObject> implements VerificationTokenService<T, CarrierObject, VerifiedActionObject> {

    private static final Logger log = LoggerFactory.getLogger(AbstractVerificationTokenService.class);
    private final EmailService<VerificationEmail> emailService;
    private final UserService userService;
    private final MongoVerificationTokenRepository<T> mongoVerificationTokenRepository;

    protected AbstractVerificationTokenService(EmailService<VerificationEmail> emailService,
                                               UserService userService,
                                               MongoVerificationTokenRepository<T> mongoVerificationTokenRepository) {
        this.emailService = emailService;
        this.userService = userService;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
    }

    protected abstract Mono<Void> sendVerificationToken(String target, String title, String token);

    protected abstract String getVerificationMessage();

    protected abstract String getVerificationLink(String token);

    @Override
    public Mono<T> getVerificationToken(String id) {
        return mongoVerificationTokenRepository.findById(id)
                .switchIfEmpty(error(InvalidVerificationTokenException::new));
    }

    @Override
    public Mono<Void> deleteVerificationToken(String id) {
        return mongoVerificationTokenRepository.deleteById(id);
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<VerifiedActionObject> performVerifiedTokenAction(T verificationToken) {
        log.info("No default action to perform, with verified token.");
        return empty();
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    protected Mono<Void> processIssue(User user, T verificationToken, Function<T, String> getTokenField, String emailTitle) {
        log.info("Issuing {} token.", verificationToken.getType().toLowerCase());
        return just(user)
                .flatMap(u -> this.saveVerificationToken(verificationToken))
                .flatMap(vt -> this.sendVerificationToken(
                        user.getEmail(),
                        emailTitle,
                        getTokenField.apply(vt)
                ));
    }

    protected Mono<T> saveVerificationToken(final T verificationToken) {
        log.info("Saving new {}", verificationToken.getType().toLowerCase());
        return mongoVerificationTokenRepository.save(verificationToken);
    }

    protected Mono<Void> sendVerificationEmail(final String email, final String title, final String token, final String verificationButtonText) {
        log.info("Sending verification email.");
        return emailService.sendEmail(
                new VerificationEmail(
                        email,
                        title,
                        this.getVerificationMessage(),
                        this.getVerificationLink(token),
                        verificationButtonText
                )
        );
    }

    protected Mono<T> validateVerificationToken(Mono<T> tokenMono) {
        return tokenMono
                .doOnNext(verificationToken -> log.info("Validating {} token.", verificationToken.getType().toLowerCase()))
                .switchIfEmpty(error(InvalidVerificationTokenException::new))
                .flatMap(verificationToken -> verificationToken.getExpiration().isBefore(LocalDateTime.now())
                        ? error(InvalidVerificationTokenException::new)
                        : just(verificationToken)
                )
                .flatMap(verificationToken -> userService.isUserExists(verificationToken.getUid())
                        .then(just(verificationToken))
                );
    }
}