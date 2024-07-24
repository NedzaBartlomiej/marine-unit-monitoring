package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.resetpassword;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenService;
import reactor.core.publisher.Mono;

public interface ResetPasswordService extends VerificationTokenService {
    Mono<Void> processResetPassword(String verificationToken, String newPassword);
}
