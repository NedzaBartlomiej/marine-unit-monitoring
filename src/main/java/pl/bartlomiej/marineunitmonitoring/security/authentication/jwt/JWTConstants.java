package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;

public class JWTConstants implements CommonFields {
    public static final String IS_VALID = "isValid";
    public static final String UID = "uid";
    public static final String REFRESH_TOKEN_TYPE = "refreshToken";
    public static final String ACCESS_TOKEN_TYPE = "accessToken";
    public static final String EMAIL_CLAIM = "email";
    public static final String TYPE_CLAIM = "type";
}
