package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.dto;

import java.time.LocalDateTime;

public class VerificationTokenReadDto {

    private String id;
    private LocalDateTime iat;
    private String type;
    private Object carrierData;

    public VerificationTokenReadDto() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getIat() {
        return iat;
    }

    public void setIat(LocalDateTime iat) {
        this.iat = iat;
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
}
