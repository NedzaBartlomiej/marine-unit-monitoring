package pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.EmailService;
import pl.bartlomiej.marineunitmonitoring.emailsending.verificationemail.VerificationEmail;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenConstants;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification.EmailVerificationToken;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

@Service
public class EmailVerificationServiceImpl extends AbstractVerificationTokenService<EmailVerificationToken, Void, Void> implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private final UserService userService;
    private final long emailTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendEmailVerificationPath;
    private final MongoVerificationTokenRepository<EmailVerificationToken> mongoVerificationTokenRepository;

    public EmailVerificationServiceImpl(
            UserService userService,
            EmailService<VerificationEmail> emailService,
            @Value("${project-properties.expiration-times.verification.email-token}") long emailTokenExpirationTime,
            @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
            @Value("${project-properties.app.frontend-integration.endpoint-paths.email-verification}") String frontendEmailVerificationPath,
            MongoVerificationTokenRepository<EmailVerificationToken> mongoVerificationTokenRepository) {
        super(emailService, userService, mongoVerificationTokenRepository);
        this.userService = userService;
        this.emailTokenExpirationTime = emailTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendEmailVerificationPath = frontendEmailVerificationPath;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
    }

    @Override
    public Mono<Void> issue(String uid, Void carrierObject) {
        return userService.getUser(uid)
                .flatMap(user -> super.processIssue(
                        user,
                        new EmailVerificationToken(
                                user.getId(),
                                this.emailTokenExpirationTime,
                                VerificationTokenType.EMAIL_VERIFICATION.name()
                        ),
                        EmailVerificationToken::getId,
                        VerificationTokenConstants.EMAIL_TITLE_APP_START + "Email verification message."
                ));
    }

    @Override
    public Mono<EmailVerificationToken> verify(String token) {
        log.info("Verifying email verification token.");
        return super.validateVerificationToken(mongoVerificationTokenRepository.findById(token))
                .flatMap(verificationToken -> userService.isUserExists(verificationToken.getUid())
                        .then(Mono.just(verificationToken))
                );
    }

    @Override
    public Mono<Void> performVerifiedTokenAction(EmailVerificationToken verificationToken) {
        log.info("Performing email verification verified token action.");
        return Mono.just(verificationToken)
                .flatMap(vt -> userService.verifyUser(vt.getUid()))
                .then(mongoVerificationTokenRepository.deleteById(verificationToken.getId()));
    }

    @Override
    protected Mono<Void> sendVerificationToken(String target, String title, String token) {
        return super.sendVerificationEmail(target, title, token, "Verify");
    }

    @Override
    protected String getVerificationMessage() {
        return "To verify your email click this link:";
    }

    @Override
    protected String getVerificationLink(String token) {
        return this.frontendUrl + this.frontendEmailVerificationPath + "/" + token;
    }
}