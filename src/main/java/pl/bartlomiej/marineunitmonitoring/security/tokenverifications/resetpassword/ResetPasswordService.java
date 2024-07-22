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
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@Service
public class ResetPasswordService extends AbstractVerificationTokenService implements VerificationTokenService<User, String> {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);
    private final UserService userService;
    private final long resetPasswordTokenExpirationTime;
    private final String frontendUrl;
    private final String frontendResetPasswordVerificationPath;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;

    public ResetPasswordService(UserService userService,
                                CustomVerificationTokenRepository customVerificationTokenRepository,
                                MongoVerificationTokenRepository mongoVerificationTokenRepository,
                                EmailService emailService,
                                @Value("${project-properties.expiration-times.verification.reset-password}") long resetPasswordTokenExpirationTime,
                                @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
                                @Value("${project-properties.app.frontend-integration.endpoint-paths.reset-password}") String frontendResetPasswordVerificationPath, MongoVerificationTokenRepository mongoVerificationTokenRepository1) {
        super(emailService, mongoVerificationTokenRepository, customVerificationTokenRepository, userService);
        this.userService = userService;
        this.resetPasswordTokenExpirationTime = resetPasswordTokenExpirationTime;
        this.frontendUrl = frontendUrl;
        this.frontendResetPasswordVerificationPath = frontendResetPasswordVerificationPath;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository1;
    }

    @Override
    public Mono<Void> issue(String email) {
        log.info("Issuing reset password verification token.");
        return userService.getUserByEmail(email)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(user -> super.saveVerificationToken(
                        new ResetPasswordVerificationToken(
                                user.getId(),
                                this.resetPasswordTokenExpirationTime,
                                VerificationTokenType.RESET_PASSWORD_VERIFICATION.name(),
                                null
                        )
                ))
                .flatMap(verificationToken -> super.sendVerificationToken(
                        verificationToken.getUid(),
                        verificationToken.getId(),
                        "Marine Unit Monitoring - reset password message."
                ));
    }

    @Override
    public Mono<User> verify(String token) {
        log.info("Verifying reset password token.");
        return mongoVerificationTokenRepository.findById(token)
                .switchIfEmpty(error()) // todo think about exception here (smth common)
                .flatMap(verificationToken -> userService.getUser(verificationToken.getUid())
                        .flatMap(user -> mongoVerificationTokenRepository.deleteById(token)
                                .thenReturn(user)
                        )
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
