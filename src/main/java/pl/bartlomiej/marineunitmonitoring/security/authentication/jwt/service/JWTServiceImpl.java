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
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
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

@Service
public class JWTServiceImpl implements JWTService {

    private static final Logger log = LoggerFactory.getLogger(JWTServiceImpl.class);
    private static final String APP_AUDIENCE_URI = "http://localhost:8080, http://localhost:3306";
    public final String tokenIssuer;
    private final String bearerPrefix;
    private final MongoJWTEntityRepository mongoJWTEntityRepository;
    private final UserService userService;
    private final int refreshTokenExpirationTime;
    private final int accessTokenExpirationTime;
    private final String secretKey;
    private final TransactionalOperator transactionalOperator;

    public JWTServiceImpl(MongoJWTEntityRepository mongoJWTEntityRepository,
                          UserService userService,
                          @Value("${project-properties.security.jwt.issuer}") String tokenIssuer,
                          @Value("${project-properties.expiration-times.jwt.refresh-token}") int refreshTokenExpirationTime,
                          @Value("${project-properties.expiration-times.jwt.access-token}") int accessTokenExpirationTime,
                          @Value("${secrets.jwt.secret-key}") String secretKey,
                          @Value("${project-properties.security.token.bearer.type}") String bearerType,
                          TransactionalOperator transactionalOperator) {
        this.bearerPrefix = bearerType + " ";
        this.mongoJWTEntityRepository = mongoJWTEntityRepository;
        this.userService = userService;
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
    public Mono<Map<String, String>> refreshAccessToken(final String refreshToken) {
        Claims claims = this.extractClaims(refreshToken);
        String subject = claims.getSubject();

        return this.performIsNotRefreshToken(refreshToken)
                .then(userService.getUser(subject))
                .flatMap(user -> this.invalidateAuthentication(refreshToken)
                        .then(this.issueTokens(user.getId(), user.getEmail()))
                );
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
                .substring(this.bearerPrefix.length());
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

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.jwt-blacklist.clearing}")
    public void clearJwtBlacklist() {
        log.info("Clearing the JWT blacklist of expired tokens.");
        mongoJWTEntityRepository.findAll()
                .filter(jwtEntity -> LocalDateTime.now().isAfter(jwtEntity.getExpiration()))
                .flatMap(mongoJWTEntityRepository::delete)
                .doOnNext(jwtEntity -> log.info("Deleted expired token from blacklist."))
                .subscribe();
    }

    private Mono<Void> performIsNotRefreshToken(final String token) {
        if (!this.getTokenType(token).equals(JWTConstants.REFRESH_TOKEN_TYPE))
            return Mono.error(new InvalidBearerTokenException("Invalid JWT."));
        else
            return Mono.empty();
    }

    private String getTokenType(final String token) {
        return this.extractClaims(token).get(JWTConstants.TYPE_CLAIM, String.class);
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
