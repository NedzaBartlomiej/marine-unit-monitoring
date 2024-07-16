package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "jwt_blacklist")
public class JWTEntity {
    private String id;
    private LocalDateTime expiration;

    public JWTEntity() {
    }

    public JWTEntity(String id, LocalDateTime expiration) {
        this.id = id;
        this.expiration = expiration;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }
}
