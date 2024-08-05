package pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.VerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.TwoFactorAuthVerificationToken;

import java.util.Map;

public interface TwoFactorAuthService extends VerificationTokenService<TwoFactorAuthVerificationToken, Void, Map<String, String>> {
}
