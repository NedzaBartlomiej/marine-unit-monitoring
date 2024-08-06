package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "jwt_tokens")
public class JWTEntity {
    private String id;
    private String uid;
    private LocalDateTime expiration;

    public JWTEntity() {
    }

    public JWTEntity(String id, String uid, LocalDateTime expiration) {
        this.id = id;
        this.uid = uid;
        this.expiration = expiration;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
