package pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.VerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification.EmailVerificationToken;

public interface EmailVerificationService extends VerificationTokenService<EmailVerificationToken, Void, Void> {
}
