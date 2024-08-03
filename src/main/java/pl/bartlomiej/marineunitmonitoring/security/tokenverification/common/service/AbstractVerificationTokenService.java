package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.InvalidVerificationTokenException;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static reactor.core.publisher.Mono.*;

@Service
public abstract class AbstractVerificationTokenService<T extends VerificationToken, CarrierObject> implements VerificationTokenService<T, CarrierObject> {

    private static final Logger log = LoggerFactory.getLogger(AbstractVerificationTokenService.class);
    private final EmailService emailService;
    private final MongoVerificationTokenRepository<T> mongoVerificationTokenRepository;

    protected AbstractVerificationTokenService(EmailService emailService,
                                               MongoVerificationTokenRepository<T> mongoVerificationTokenRepository) {
        this.emailService = emailService;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
    }

    protected abstract Mono<Void> sendVerificationToken(String target, String title, String token);

    protected abstract String buildVerificationMessage(String verificationUrl);

    protected abstract String buildVerificationUrl(String token);

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
    public Mono<Void> performVerifiedTokenAction(T verificationToken) {
        log.info("No default action to perform, with verified token.");
        return empty();
    }

    protected Mono<Void> processIssue(User user, T verificationToken, String emailTitle) {
        log.info("Issuing {} token.", verificationToken.getType().toLowerCase());
        return just(user)
                .flatMap(u -> this.saveVerificationToken(verificationToken))
                .flatMap(vt -> this.sendVerificationToken(
                        user.getEmail(),
                        emailTitle,
                        vt.getId()
                ));
    }

    protected Mono<T> saveVerificationToken(T verificationToken) {
        log.info("Saving new {}", verificationToken.getType().toLowerCase());
        return mongoVerificationTokenRepository.save(verificationToken);
    }

    protected Mono<Void> sendVerificationEmail(String email, String title, String token) {
        log.info("Sending verification email.");
        return emailService.sendEmail(
                email,
                title,
                this.buildVerificationMessage(this.buildVerificationUrl(token))
        );
    }

    protected Mono<T> validateVerificationToken(Mono<T> tokenMono) {
        return tokenMono
                .doOnNext(verificationToken -> log.info("Validating {} token.", verificationToken.getType().toLowerCase()))
                .switchIfEmpty(error(InvalidVerificationTokenException::new))
                .flatMap(verificationToken -> verificationToken.getExpiration().isBefore(LocalDateTime.now())
                        ? error(InvalidVerificationTokenException::new)
                        : just(verificationToken)
                );
    }
}