package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Document(collection = "verification_tokens")
public abstract class VerificationToken {
    private String id;
    private String uid;
    private LocalDateTime expiration;
    private LocalDateTime iat;
    private String type;

    public VerificationToken() {
    }

    public VerificationToken(String uid, long expirationTime, String type) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.expiration = LocalDateTime.now().plus(expirationTime, ChronoUnit.MILLIS);
        this.iat = LocalDateTime.now();
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getIat() {
        return iat;
    }

    public void setIat(LocalDateTime iat) {
        this.iat = iat;
    }
}
