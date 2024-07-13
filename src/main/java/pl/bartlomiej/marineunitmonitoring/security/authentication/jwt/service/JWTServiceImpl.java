package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.MongoJWTEntityRepository;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTServiceImpl implements JWTService {
    public static final String TOKEN_ISSUER = "marine-unit-monitoring";
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    public static final int ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String APP_AUDIENCE_URI = "http://localhost:8080, http://localhost:3306";
    private final MongoJWTEntityRepository jwtEntityRepository;
    @Value("${secrets.jwt.secret-key}")
    private String SECRET_KEY;

    public JWTServiceImpl(MongoJWTEntityRepository jwtEntityRepository) {
        this.jwtEntityRepository = jwtEntityRepository;
    }

    public String createAccessToken(String uid, String email) {
        Map<String, String> accessTokenCustomClaims = Map.of(
                "email", email
        );
        return this.buildToken(uid, accessTokenCustomClaims, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String createRefreshToken(String uid, String email) {
        Map<String, String> refreshTokenCustomClaims = Map.of(
                "email", email
        );
        return this.buildToken(uid, refreshTokenCustomClaims, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    @Override
    public String refreshAccessToken(String token) {
        // verify refreshToken - write handling in globalAdvice
        // get user by token subject
        // this.createAccessToken()
        return "";
    }

    @Override
    public Mono<Void> invalidate(String token) {
        Claims claims = this.extractClaims(token);
        // there is no checking isExist, because it will be checked in the JWTBlacklistVerifier
        return jwtEntityRepository.save(new JWTEntity(claims.getId())
        ).then();
    }

    @Override
    public Mono<Boolean> isBlacklisted(String jti) {
        return jwtEntityRepository.existsById(jti);
    }

    public String extract(ServerWebExchange exchange) {

        final String authorizationHeaderValue = exchange
                .getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderValue == null) {
            return "";
        }

        return authorizationHeaderValue
                .substring(BEARER_PREFIX.length());
    }

    public String getJti(String token) {
        return this.extractClaims(token).getId();
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(String uid, Map<String, String> customClaims, int expirationTime) {
        return Jwts.builder()
                .setClaims(customClaims)
                .setId(UUID.randomUUID().toString())
                .setIssuer(TOKEN_ISSUER)
                .setSubject(uid)
                .setAudience(APP_AUDIENCE_URI)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(this.getSigningKey())
                .compact();
    }

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
