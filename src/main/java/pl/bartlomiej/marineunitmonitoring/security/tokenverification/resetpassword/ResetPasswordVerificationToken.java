package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

public class ResetPasswordVerificationToken extends VerificationToken {

    private Boolean isVerified;

    public ResetPasswordVerificationToken() {
        super();
    }

    public ResetPasswordVerificationToken(String uid, long expirationTime, String type) {
        super(uid, expirationTime, type);
        this.isVerified = false;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }
}
