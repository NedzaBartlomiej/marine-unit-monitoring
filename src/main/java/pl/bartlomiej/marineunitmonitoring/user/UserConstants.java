package pl.bartlomiej.marineunitmonitoring.user;

import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;

public final class UserConstants implements CommonFields {

    public static final String TRACKED_SHIPS = "trackedShips";
    public static final String EMAIL = "email";
    public static final String OPEN_IDS = "openIds";
    public static final String IS_VERIFIED = "isVerified";
    public static final String IS_LOCKED = "isLocked";
    public static final String PASSWORD = "password";
    public static final String TRUSTED_IP_ADDRESSES = "trustedIpAddresses";
    public static final String IS_TWO_FACTOR_AUTH_ENABLED = "isTwoFactorAuthEnabled";

    private UserConstants() {
    }
}
