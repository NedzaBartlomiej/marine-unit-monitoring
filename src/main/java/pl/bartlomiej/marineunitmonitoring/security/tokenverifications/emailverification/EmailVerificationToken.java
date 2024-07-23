package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

import java.time.LocalDateTime;

public class EmailVerificationToken extends VerificationToken {

    public EmailVerificationToken(String uid, long expirationTime, String type) {
        super(
                uid,
                LocalDateTime.now().plusHours(expirationTime),
                type
        );
    }
}
