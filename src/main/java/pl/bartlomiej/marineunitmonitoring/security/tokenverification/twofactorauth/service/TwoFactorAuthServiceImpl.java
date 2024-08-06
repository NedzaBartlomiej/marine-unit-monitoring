package pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenConstants;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.MongoTwoFactorAuthVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.TwoFactorAuthVerificationToken;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class TwoFactorAuthServiceImpl extends AbstractVerificationTokenService<TwoFactorAuthVerificationToken, Void, Map<String, String>> implements TwoFactorAuthService {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorAuthServiceImpl.class);
    private final MongoTwoFactorAuthVerificationTokenRepository mongoVerificationTokenRepository;
    private final UserService userService;
    private final JWTService jwtService;
    private final long twoFactorAuthTokenExpirationTime;

    protected TwoFactorAuthServiceImpl(EmailService emailService,
                                       MongoTwoFactorAuthVerificationTokenRepository mongoVerificationTokenRepository,
                                       UserService userService,
                                       @Value("${project-properties.expiration-times.verification.two-fa-token}") long twoFactorAuthTokenExpirationTime, JWTService jwtService) {
        super(emailService, userService, mongoVerificationTokenRepository);
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.userService = userService;
        this.twoFactorAuthTokenExpirationTime = twoFactorAuthTokenExpirationTime;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> issue(String uid, Void unused) {
        return userService.getUser(uid)
                .flatMap(user -> super.processIssue(
                        user,
                        new TwoFactorAuthVerificationToken(
                                user.getId(),
                                twoFactorAuthTokenExpirationTime,
                                VerificationTokenType.TWO_FACTOR_AUTH_VERIFICATION.name()
                        ),
                        TwoFactorAuthVerificationToken::getCode,
                        VerificationTokenConstants.EMAIL_TITLE_APP_START + "Two factor authentication message."
                ));
    }

    @Override
    public Mono<TwoFactorAuthVerificationToken> verify(String code) {
        log.info("Verifying two factor auth token.");
        return super.validateVerificationToken(mongoVerificationTokenRepository.findByCode(code));
    }

    @Override
    public Mono<Map<String, String>> performVerifiedTokenAction(TwoFactorAuthVerificationToken verificationToken) {
        log.info("Performing two factor auth verified token action:");
        return mongoVerificationTokenRepository.delete(verificationToken)
                .then(userService.getUser(verificationToken.getUid())
                        .flatMap(user -> jwtService.issueTokens(user.getId(), user.getEmail()))
                );
    }

    @Override
    protected Mono<Void> sendVerificationToken(String target, String title, String token) {
        return super.sendVerificationEmail(target, title, token);
    }

    @Override
    protected String buildVerificationMessage(String verificationItem) {
        return "Your two factor authentication code: " + verificationItem;
    }

    @Override
    protected String buildVerificationItem(String token) {
        return token;
    }
}
