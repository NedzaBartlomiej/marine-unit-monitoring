package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

public class IpAuthProtectionVerificationToken extends VerificationToken {
    public IpAuthProtectionVerificationToken() {
    }

    public IpAuthProtectionVerificationToken(String uid, long expirationTime, String type, Object carrierData) {
        super(uid, expirationTime, type, carrierData);
    }
}
