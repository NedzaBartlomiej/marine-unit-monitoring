package pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.dto;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.dto.VerificationTokenDto;

import java.time.LocalDateTime;

public class IpAuthProtectionVTReadDto implements VerificationTokenDto {
    private LocalDateTime expiration;
    private LocalDateTime iat;
    private String ipAddress;

    public IpAuthProtectionVTReadDto() {
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public LocalDateTime getIat() {
        return iat;
    }

    public void setIat(LocalDateTime iat) {
        this.iat = iat;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
