package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common;

import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;

public final class VerificationTokenConstants implements CommonFields {
    public static final String EXPIRATION = "expiration";
    public static final String IS_VERIFIED = "isVerified";
    public static final String EMAIL_TITLE_APP_START = "Marine Unit Monitoring - ";

    private VerificationTokenConstants() {
    }
}
