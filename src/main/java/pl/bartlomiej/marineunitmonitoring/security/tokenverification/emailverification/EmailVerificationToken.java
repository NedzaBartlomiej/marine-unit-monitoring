package pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

public class EmailVerificationToken extends VerificationToken {

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
    }
}
