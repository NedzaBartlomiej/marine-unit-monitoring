package pl.bartlomiej.marineunitmonitoring.security.emailverification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "email_verifications")
public class EmailVerificationEntity {
    private String id;
    private String uid;
    @Value("${project-properties.expiration-times.verification.email-token}")
    private long expirationTime;
    private LocalDateTime expiration;

    public EmailVerificationEntity() {
    }

    public EmailVerificationEntity(String uid) {
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
}
