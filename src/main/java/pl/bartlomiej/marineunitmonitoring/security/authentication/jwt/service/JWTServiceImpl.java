package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.MongoJWTEntityRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.CommonCustomTokenClaim.EMAIL;
import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.CommonCustomTokenClaim.TYPE;
import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.JWTType.ACCESS_TOKEN;
import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.JWTType.REFRESH_TOKEN;
import static reactor.core.publisher.Mono.just;

@Service
public class JWTServiceImpl implements JWTService {

    private static final Logger log = LoggerFactory.getLogger(JWTServiceImpl.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String APP_AUDIENCE_URI = "http://localhost:8080, http://localhost:3306";
    @Value("${project-properties.security.jwt.issuer}")
    public static String TOKEN_ISSUER;
    private final MongoJWTEntityRepository mongoJWTEntityRepository;
    private final UserService userService;
    @Value("${project-properties.expiration-times.jwt.refresh-token}")
    private int refreshTokenExpirationTime;
    @Value("${project-properties.expiration-times.jwt.access-token}")
    private int accessTokenExpirationTime;
    @Value("${secrets.jwt.secret-key}")
    private String SECRET_KEY;

    public JWTServiceImpl(MongoJWTEntityRepository mongoJWTEntityRepository, UserService userService) {
        this.mongoJWTEntityRepository = mongoJWTEntityRepository;
        this.userService = userService;
    }

    public String createAccessToken(String uid, String email) {
        final Map<String, String> accessTokenCustomClaims = Map.of(
                EMAIL.getClaim(), email,
                TYPE.getClaim(), ACCESS_TOKEN.getType()
        );
        return this.buildToken(uid, accessTokenCustomClaims, accessTokenExpirationTime);
    }

    public String createRefreshToken(String uid, String email) {
        final Map<String, String> refreshTokenCustomClaims = Map.of(
                EMAIL.getClaim(), email,
                TYPE.getClaim(), REFRESH_TOKEN.getType()
        );
        return this.buildToken(uid, refreshTokenCustomClaims, refreshTokenExpirationTime);
    }

    @Override
    public Mono<Map<String, String>> refreshAccessToken(String refreshToken) {
        Claims claims = this.extractClaims(refreshToken);
        String tokenType = (String) claims.get(TYPE.getClaim());
        String subject = claims.getSubject();

        if (!tokenType.equals(REFRESH_TOKEN.getType())) throw new InvalidBearerTokenException("Invalid JWT.");

        return userService.getUser(subject)
                .flatMap(user -> this.invalidate(refreshToken)
                        .then(just(
                                Map.of(
                                        ACCESS_TOKEN.getType(), this.createAccessToken(user.getId(), user.getEmail()),
                                        REFRESH_TOKEN.getType(), this.createRefreshToken(user.getId(), user.getEmail())
                                ))
                        )
                );
    }

    @Override
    public Mono<Void> invalidate(String token) {
        Claims claims = this.extractClaims(token);
        // here there is no checking isExist, because it will be checked in the JWTBlacklistVerifier
        return mongoJWTEntityRepository.save(
                new JWTEntity(claims.getId(),
                        LocalDateTime.ofInstant(
                                claims.getExpiration().toInstant(),
                                ZoneId.systemDefault()
                        )
                )
        ).then();
    }

    @Override
    public Mono<Boolean> isBlacklisted(String jti) {
        return mongoJWTEntityRepository.existsById(jti);
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

    public Claims extractClaims(String token) {
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

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.jwt-blacklist.clearing}")
    public void clearJwtBlacklist() {
        log.info("Clearing the JWT blacklist of expired tokens.");
        mongoJWTEntityRepository.findAll()
                .filter(jwtEntity -> LocalDateTime.now().isAfter(jwtEntity.getExpiration()))
                .flatMap(mongoJWTEntityRepository::delete)
                .doOnNext(jwtEntity -> log.info("Deleted expired token from blacklist."))
                .subscribe();
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


    public enum CommonCustomTokenClaim {
        EMAIL("email"), TYPE("type");

        private final String claim;

        CommonCustomTokenClaim(String claim) {
            this.claim = claim;
        }

        public String getClaim() {
            return claim;
        }
    }

    public enum JWTType {
        REFRESH_TOKEN("refreshToken"), ACCESS_TOKEN("accessToken");

        private final String type;

        JWTType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
