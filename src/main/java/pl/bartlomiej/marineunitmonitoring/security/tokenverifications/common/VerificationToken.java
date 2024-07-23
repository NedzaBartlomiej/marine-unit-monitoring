package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "verification_tokens")
public abstract class VerificationToken {
    private String id;
    private String uid;
    private LocalDateTime expiration;
    private String type;
    private Object carrierData;
    private Boolean isVerified;

    public VerificationToken() {
    }

    public VerificationToken(String uid, LocalDateTime expiration, String type, Object carrierData) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.expiration = expiration;
        this.type = type;
        this.carrierData = carrierData;
    }

    public VerificationToken(String uid, LocalDateTime expiration, String type) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.expiration = expiration;
        this.type = type;
        this.carrierData = null;
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

    public Object getCarrierData() {
        return carrierData;
    }

    public void setCarrierData(Object carrierData) {
        this.carrierData = carrierData;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }
}
