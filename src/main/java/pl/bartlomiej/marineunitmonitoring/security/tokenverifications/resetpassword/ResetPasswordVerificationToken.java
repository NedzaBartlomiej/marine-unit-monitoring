package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.resetpassword;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

import java.time.LocalDateTime;

public class ResetPasswordVerificationToken extends VerificationToken {
    public ResetPasswordVerificationToken(String uid, long expirationTime, String type) {
        super(
                uid,
                LocalDateTime.now().plusHours(expirationTime),
                type
        );
    }
}
