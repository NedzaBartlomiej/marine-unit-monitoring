package pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

@Document("verification_tokens")
public class TwoFactorAuthVerificationToken extends VerificationToken {

    private String code;

    public TwoFactorAuthVerificationToken() {
    }

    public TwoFactorAuthVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
        this.code = RandomStringUtils.random(5, false, true);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
