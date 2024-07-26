package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

public class IpAddressVerificationToken extends VerificationToken {
    public IpAddressVerificationToken() {
    }

    public IpAddressVerificationToken(String uid, long expirationTime, String type, Object carrierData) {
        super(uid, expirationTime, type, carrierData);
    }
}
