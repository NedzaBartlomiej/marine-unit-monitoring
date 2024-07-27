package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.VerificationTokenService;
import reactor.core.publisher.Mono;

public interface ResetPasswordService extends VerificationTokenService {
    Mono<Void> processResetPassword(String verificationToken, String newPassword);
}
