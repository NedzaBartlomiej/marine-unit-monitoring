package pl.bartlomiej.marineunitmonitoring.security.emailverification;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "email_verifications")
public class EmailVerificationEntity {
    private String id;
    private String uid;
    private LocalDateTime expiration;

    public EmailVerificationEntity() {
    }

    public EmailVerificationEntity(String uid, long expirationTime) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.expiration = LocalDateTime.now().plusHours(expirationTime);
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }
}
