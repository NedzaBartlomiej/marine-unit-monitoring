package pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification;

import org.springframework.data.mongodb.core.mapping.Document;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

@Document(collection = "verification_tokens")
public class EmailVerificationToken extends VerificationToken {

    public EmailVerificationToken() {
        super();
    }

    public EmailVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
    }
}
