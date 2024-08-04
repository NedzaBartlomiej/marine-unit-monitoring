package pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.service;

import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.TwoFactorAuthVerificationToken;
import reactor.core.publisher.Mono;

public class TwoFactorAuthServiceImpl extends AbstractVerificationTokenService<TwoFactorAuthVerificationToken, Void> implements TwoFactorAuthService {

    private final MongoVerificationTokenRepository<TwoFactorAuthVerificationToken> mongoVerificationTokenRepository;

    protected TwoFactorAuthServiceImpl(EmailService emailService,
                                       MongoVerificationTokenRepository<TwoFactorAuthVerificationToken> mongoVerificationTokenRepository) {
        super(emailService, mongoVerificationTokenRepository);
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
    }

    @Override
    public Mono<Void> issue(String identifier, Void unused) {

    }

    @Override
    public Mono<TwoFactorAuthVerificationToken> verify(String token) {

    }

    @Override
    public Mono<Void> performVerifiedTokenAction(TwoFactorAuthVerificationToken verificationToken) {

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
