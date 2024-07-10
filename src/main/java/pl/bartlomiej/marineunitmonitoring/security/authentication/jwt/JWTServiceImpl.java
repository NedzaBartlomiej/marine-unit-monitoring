package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTServiceImpl implements JWTService {
    public static final String TOKEN_ISSUER = "marine-unit-monitoring";
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    public static final int ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60;
    private static final String APP_AUDIENCE_URI = "http://localhost:8080, http://localhost:3306";
    @Value("${secrets.jwt.secret-key}")
    private String SECRET_KEY;

    @Override
    public String createAccessToken(String uid, String email) {
        final Map<String, String> userInfoClaims = new HashMap<>();
        userInfoClaims.put("email", email);

        return Jwts.builder()
                .setClaims(userInfoClaims)
                .setId(UUID.randomUUID().toString())
                .setIssuer(TOKEN_ISSUER)
                .setSubject(uid)
                .setAudience(APP_AUDIENCE_URI)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(this.getSigningKey())
                .compact();
    }

    @Override
    public String createRefreshToken(String uid) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(TOKEN_ISSUER)
                .setSubject(uid)
                .setAudience(APP_AUDIENCE_URI)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(this.getSigningKey())
                .compact();
    }

    @Override
    public String invalidate(String token) {
        Claims claims = extractClaims(token);
        claims.setExpiration(claims.getIssuedAt());
        return Jwts.builder()
                .setClaims(claims)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
