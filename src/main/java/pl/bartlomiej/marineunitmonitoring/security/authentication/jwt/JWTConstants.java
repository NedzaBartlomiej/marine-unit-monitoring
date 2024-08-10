package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;

public class JWTConstants implements CommonFields {
    public static final String ISSUE_ID = "issueId";
    public static final String REFRESH_TOKEN_TYPE = "refreshToken";
    public static final String ACCESS_TOKEN_TYPE = "accessToken";
    public static final String EMAIL_CLAIM = "email";
    public static final String TYPE_CLAIM = "type";
    public static final String SUBJECT_CLAIM = "sub";
    public static final String BEARER_REGEX = "^Bearer\\s+(\\S+)";
    public static final String BEARER_TYPE = "Bearer ";
}
