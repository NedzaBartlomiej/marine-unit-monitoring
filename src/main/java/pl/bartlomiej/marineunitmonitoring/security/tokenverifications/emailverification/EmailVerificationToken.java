package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

public class EmailVerificationToken extends VerificationToken {

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
    }
}
