package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import java.security.Key;

public interface JWTService {

    String createAccessToken(String uid, String email);

    String createRefreshToken(String uid);

    String invalidate(String token);

    Key getSigningKey();
}
