package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service
public class EmailVerificationService extends AbstractVerificationTokenService implements VerificationTokenService<Void, String> {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private final UserService userService;
    private final long emailTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendEmailVerificationPath;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;

    public EmailVerificationService(
            UserService userService,
            EmailService emailService,
            @Value("${project-properties.expiration-times.verification.email-token}") long emailTokenExpirationTime,
            @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
            @Value("${project-properties.app.frontend-integration.endpoint-paths.email-verification}") String frontendEmailVerificationPath,
            CustomVerificationTokenRepository customVerificationTokenRepository,
            MongoVerificationTokenRepository mongoVerificationTokenRepository) {
        super(emailService, mongoVerificationTokenRepository, customVerificationTokenRepository, userService);
        this.userService = userService;
        this.emailTokenExpirationTime = emailTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendEmailVerificationPath = frontendEmailVerificationPath;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
    }

    @Override
    public Mono<Void> issue(String uid) {
        log.info("Issuing email verification token.");
        return userService.getUser(uid)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(user -> super.saveVerificationToken(
                        new EmailVerificationToken(
                                user.getId(),
                                this.emailTokenExpirationTime,
                                VerificationTokenType.EMAIL_VERIFICATION.name(),
                                null
                        )
                ))
                .flatMap(verificationToken -> super.sendVerificationToken(
                        verificationToken.getUid(),
                        verificationToken.getId(),
                        "Marine Unit Monitoring - verification email message."
                ));
    }

    @Override
    public Mono<Void> verify(String token) {
        log.info("Verifying email verification token.");
        return mongoVerificationTokenRepository.findById(token)
                .switchIfEmpty(error()) // todo think about exception here (smth common)
                .flatMap(verificationToken -> verificationToken.getExpiration().isBefore(LocalDateTime.now())
                        ? error(ExpiredVerificationTokenException::new) // todo or resend or smth - to think about
                        : just(verificationToken)
                )
                .flatMap(verificationToken -> userService.getUser(verificationToken.getUid()))
                .flatMap(user -> userService.verifyUser(user.getId()))
                .then(mongoVerificationTokenRepository.deleteById(token));
    }

    protected String buildVerificationMessage(String verificationUrl) {
        return "To verify your email click this link: " + verificationUrl;
    }

    protected String buildVerificationUrl(String token) {
        return this.frontendUrl + this.frontendEmailVerificationPath + "/" + token;
    }
}