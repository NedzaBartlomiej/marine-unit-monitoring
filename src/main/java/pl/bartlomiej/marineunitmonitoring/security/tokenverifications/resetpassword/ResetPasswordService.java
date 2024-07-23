package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.resetpassword;

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

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

// todo - test
@Service
public class ResetPasswordService extends AbstractVerificationTokenService implements VerificationTokenService {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);
    private final UserService userService;
    private final long resetPasswordTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendResetPasswordVerificationPath;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;
    private final CustomVerificationTokenRepository customVerificationTokenRepository;

    public ResetPasswordService(UserService userService,
                                CustomVerificationTokenRepository customVerificationTokenRepository,
                                MongoVerificationTokenRepository mongoVerificationTokenRepository,
                                EmailService emailService,
                                @Value("${project-properties.expiration-times.verification.reset-password}") long resetPasswordTokenExpirationTime,
                                @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
                                @Value("${project-properties.app.frontend-integration.endpoint-paths.reset-password}") String frontendResetPasswordVerificationPath) {
        super(emailService, mongoVerificationTokenRepository, customVerificationTokenRepository, userService);
        this.userService = userService;
        this.resetPasswordTokenExpirationTime = resetPasswordTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendResetPasswordVerificationPath = frontendResetPasswordVerificationPath;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.customVerificationTokenRepository = customVerificationTokenRepository;
    }

    /**
     * @throws NotFoundException when the user is based only on OAuth2 data (when the user isn't created by registration)
     */
    @Override
    public Mono<Void> issue(String email) {
        return userService.getUserByEmail(email)
                .flatMap(user -> {
                    if (user.getPassword() == null) {
                        return error(NotFoundException::new);
                    }
                    return just(user);
                })
                .flatMap(user -> super.issue(
                        user,
                        new ResetPasswordVerificationToken(
                                user.getId(),
                                this.resetPasswordTokenExpirationTime,
                                VerificationTokenType.RESET_PASSWORD_VERIFICATION.name()
                        ),
                        "Marine Unit Monitoring - reset password message."
                ));
    }

    @Override
    public Mono<Void> verify(String token) {
        log.info("Verifying reset password token.");
        return mongoVerificationTokenRepository.findById(token)
                .flatMap(super::validateVerificationToken)
                .flatMap(verificationToken -> userService.isUserExists(verificationToken.getUid())
                        .then(just(verificationToken))
                )
                .flatMap(verificationToken -> customVerificationTokenRepository
                        .updateIsVerified(verificationToken.getId(), true)
                );
    }

    @Override
    protected String buildVerificationMessage(String verificationUrl) {
        return "To reset password click this link: " + verificationUrl;
    }

    @Override
    protected String buildVerificationUrl(String token) {
        return this.frontendUrl + this.frontendResetPasswordVerificationPath + "/" + token;
    }
}
