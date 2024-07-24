package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.resetpassword;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

public class ResetPasswordVerificationToken extends VerificationToken {

    public ResetPasswordVerificationToken() {
    }

    public ResetPasswordVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
    }
}
