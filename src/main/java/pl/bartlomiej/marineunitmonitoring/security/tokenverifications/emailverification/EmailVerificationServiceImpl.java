package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

@Service
public class EmailVerificationServiceImpl extends AbstractVerificationTokenService implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private final UserService userService;
    private final long emailTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendEmailVerificationPath;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;

    public EmailVerificationServiceImpl(
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
        return userService.getUser(uid)
                .flatMap(user -> super.issue(
                        user,
                        new EmailVerificationToken(
                                user.getId(),
                                this.emailTokenExpirationTime,
                                VerificationTokenType.EMAIL_VERIFICATION.name()
                        ),
                        "Marine Unit Monitoring - verification email message."
                ));
    }

    @Override
    public Mono<Void> verify(String token) {
        log.info("Verifying email verification token.");
        return super.validateVerificationToken(mongoVerificationTokenRepository.findById(token))
                .flatMap(verificationToken -> userService.getUser(verificationToken.getUid())) // user may not exist -> NotFoundEx then
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