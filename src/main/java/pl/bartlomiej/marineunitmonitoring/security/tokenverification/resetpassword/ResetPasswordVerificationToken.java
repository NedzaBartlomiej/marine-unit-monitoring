package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

public class ResetPasswordVerificationToken extends VerificationToken {

    public ResetPasswordVerificationToken() {
    }

    public ResetPasswordVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
    }
}
