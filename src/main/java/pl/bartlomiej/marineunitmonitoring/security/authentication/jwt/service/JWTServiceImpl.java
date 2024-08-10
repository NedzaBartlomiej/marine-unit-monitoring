package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.InvalidJWTException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.MongoJWTEntityRepository;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTServiceImpl implements JWTService {

    private static final Logger log = LoggerFactory.getLogger(JWTServiceImpl.class);
    private static final String APP_AUDIENCE_URI = "http://localhost:8080, http://localhost:3306";
    public final String tokenIssuer;
    private final MongoJWTEntityRepository mongoJWTEntityRepository;
    private final int refreshTokenExpirationTime;
    private final int accessTokenExpirationTime;
    private final String secretKey;
    private final TransactionalOperator transactionalOperator;

    public JWTServiceImpl(MongoJWTEntityRepository mongoJWTEntityRepository,
                          @Value("${jwt.issuer}") String tokenIssuer,
                          @Value("${jwt.expiration.refresh-token}") int refreshTokenExpirationTime,
                          @Value("${jwt.expiration.access-token}") int accessTokenExpirationTime,
                          @Value("${jwt.secret-key}") String secretKey,
                          TransactionalOperator transactionalOperator) {
        this.mongoJWTEntityRepository = mongoJWTEntityRepository;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.secretKey = secretKey;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Map<String, String>> issueTokens(final String uid, final String email) {
        final String issueId = UUID.randomUUID().toString();
        return transactionalOperator.transactional(
                this.invalidateAll(uid)
                        .then(Mono.zip(this.issueRefreshToken(uid, email, issueId), this.issueAccessToken(uid, email, issueId),
                                (refreshToken, accessToken) -> Map.of(
                                        JWTConstants.REFRESH_TOKEN_TYPE, refreshToken,
                                        JWTConstants.ACCESS_TOKEN_TYPE, accessToken
                                )
                        ))
        );
    }

    public Mono<String> issueAccessToken(final String uid, final String email, final String issueId) {
        final Map<String, String> accessTokenCustomClaims = Map.of(
                JWTConstants.EMAIL_CLAIM, email,
                JWTConstants.TYPE_CLAIM, JWTConstants.ACCESS_TOKEN_TYPE,
                JWTConstants.ISSUE_ID, issueId
        );
        return this.issueToken(uid, accessTokenCustomClaims, accessTokenExpirationTime, issueId);
    }

    public Mono<String> issueRefreshToken(final String uid, final String email, final String issueId) {
        final Map<String, String> refreshTokenCustomClaims = Map.of(
                JWTConstants.EMAIL_CLAIM, email,
                JWTConstants.TYPE_CLAIM, JWTConstants.REFRESH_TOKEN_TYPE,
                JWTConstants.ISSUE_ID, issueId
        );
        return this.issueToken(uid, refreshTokenCustomClaims, refreshTokenExpirationTime, issueId);
    }

    @Override
    public Mono<Map<String, String>> refreshAccessToken(final String refreshToken, final String uid, final String email) {
        return this.performIsNotRefreshToken(refreshToken)
                .then(this.invalidateAuthentication(refreshToken))
                .then(this.issueTokens(uid, email));
    }

    @Override
    public Mono<Boolean> isValid(final String token) {
        Claims claims = this.extractClaims(token);
        return mongoJWTEntityRepository.existsById(claims.getId());
    }

    @Override
    public Mono<Void> invalidateAuthentication(final String refreshToken) {
        Claims claims = this.extractClaims(refreshToken);
        log.info("Invalidating authentication.");
        return this.performIsNotRefreshToken(refreshToken)
                .then(mongoJWTEntityRepository.deleteByIssueId(claims.get(JWTConstants.ISSUE_ID, String.class)));
    }

    @Override
    public Mono<Void> invalidateAll(final String uid) {
        log.info("Invalidation of all user's JWTs.");
        return mongoJWTEntityRepository.deleteAllByUid(uid);
    }

    public String extract(final ServerWebExchange exchange) {
        final String authorizationHeaderValue = exchange
                .getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderValue == null) {
            return "";
        }

        return authorizationHeaderValue
                .substring(JWTConstants.BEARER_TYPE.length());
    }

    public Claims extractClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${jwt.store-clearing}")
    public void clearJwtBlacklist() {
        log.info("Clearing the JWT blacklist of expired tokens.");
        mongoJWTEntityRepository.findAll()
                .filter(jwtEntity -> LocalDateTime.now().isAfter(jwtEntity.getExpiration()))
                .flatMap(mongoJWTEntityRepository::delete)
                .doOnNext(jwtEntity -> log.info("Deleted expired token from blacklist."))
                .subscribe();
    }

    private Mono<Void> performIsNotRefreshToken(final String token) {
        if (!this.extractTokenType(token).equals(JWTConstants.REFRESH_TOKEN_TYPE))
            return Mono.error(new InvalidJWTException());
        else
            return Mono.empty();
    }

    private String extractTokenType(final String token) {
        return this.extractClaims(token).get(JWTConstants.TYPE_CLAIM, String.class);
    }

    @Override
    public String extractSubject(final String token) {
        return this.extractClaims(token).get(JWTConstants.SUBJECT_CLAIM, String.class);
    }

    @Override
    public String extractEmail(final String token) {
        return this.extractClaims(token).get(JWTConstants.EMAIL_CLAIM, String.class);
    }

    private Mono<String> issueToken(final String uid, final Map<String, String> customClaims, final int expirationTime, final String issueId) {
        log.info("Issuing new JWT.");
        final String jti = UUID.randomUUID().toString();
        final Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        JWTEntity jwtEntity = new JWTEntity(
                jti,
                uid,
                issueId,
                LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault())
        );

        log.info("Saving new JWT.");
        return mongoJWTEntityRepository.save(jwtEntity)
                .then(Mono.fromCallable(() -> this.buildToken(customClaims, jti, uid, expiration)))
                .doOnSuccess(token -> log.info("JWT successfully issued."))
                .doOnError(error -> log.error("Failed to issue JWT: {}", error.getMessage()))
                .as(transactionalOperator::transactional);
    }

    private String buildToken(final Map<String, String> customClaims, final String jti, final String uid, final Date expiration) {
        log.info("Building new JWT.");
        return Jwts.builder()
                .setClaims(customClaims)
                .setIssuer(this.tokenIssuer)
                .setId(jti)
                .setSubject(uid)
                .setAudience(APP_AUDIENCE_URI)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(this.getSigningKey())
                .compact();
    }
}
