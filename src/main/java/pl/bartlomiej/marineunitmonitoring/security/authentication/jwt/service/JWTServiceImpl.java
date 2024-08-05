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
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository.CustomJWTEntityRepository;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository.MongoJWTEntityRepository;
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
    private final CustomJWTEntityRepository customJWTEntityRepository;
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
                          CustomJWTEntityRepository customJWTEntityRepository, TransactionalOperator transactionalOperator) {
        this.bearerPrefix = bearerType + " ";
        this.mongoJWTEntityRepository = mongoJWTEntityRepository;
        this.userService = userService;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.secretKey = secretKey;
        this.customJWTEntityRepository = customJWTEntityRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Map<String, String>> createTokenPacket(String uid, String email) {
        return Mono.zip(this.createRefreshToken(uid, email), this.createAccessToken(uid, email),
                (refreshToken, accessToken) ->
                        Map.of(
                                JWTConstants.REFRESH_TOKEN_TYPE, refreshToken,
                                JWTConstants.ACCESS_TOKEN_TYPE, accessToken
                        )
        );
    }

    @Override
    public Mono<String> createAccessToken(String uid, String email) {
        final Map<String, String> accessTokenCustomClaims = Map.of(
                JWTConstants.EMAIL_CLAIM, email,
                JWTConstants.TYPE_CLAIM, JWTConstants.ACCESS_TOKEN_TYPE
        );
        return this.issueToken(uid, accessTokenCustomClaims, accessTokenExpirationTime);
    }

    @Override
    public Mono<String> createRefreshToken(String uid, String email) {
        final Map<String, String> refreshTokenCustomClaims = Map.of(
                JWTConstants.EMAIL_CLAIM, email,
                JWTConstants.TYPE_CLAIM, JWTConstants.REFRESH_TOKEN_TYPE
        );
        return this.issueToken(uid, refreshTokenCustomClaims, refreshTokenExpirationTime);
    }

    @Override
    public Mono<Map<String, String>> refreshAccessToken(String refreshToken) {
        Claims claims = this.extractClaims(refreshToken);
        String tokenType = (String) claims.get(JWTConstants.TYPE_CLAIM);
        String subject = claims.getSubject();

        if (!tokenType.equals(JWTConstants.REFRESH_TOKEN_TYPE)) throw new InvalidBearerTokenException("Invalid JWT.");

        return userService.getUser(subject)
                .flatMap(user -> this.invalidate(refreshToken)
                        .then(this.createTokenPacket(user.getId(), user.getEmail()))
                );
    }

    @Override
    public Mono<Boolean> isValid(String token) {
        Claims claims = this.extractClaims(token);
        return mongoJWTEntityRepository.findById(claims.getId())
                .map(JWTEntity::getValid);
    }

    @Override
    public Mono<Void> invalidate(String token) {
        Claims claims = this.extractClaims(token);
        log.info("Invalidating JWT.");
        return customJWTEntityRepository.updateIsValid(claims.getId(), false);
    }

    @Override
    public Mono<Void> invalidateAll(String uid) {
        log.info("Invalidation of all user's JWTs.");
        return customJWTEntityRepository.updateIsValidByUid(uid, false);
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
                .substring(this.bearerPrefix.length());
    }

    public Claims extractClaims(String token) {
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

    // todo - when issuing new token - invalidate all previous user tokens (impl it as transaction with issuing)
    private Mono<String> issueToken(String uid, Map<String, String> customClaims, int expirationTime) {
        log.info("Issuing new JWT.");
        final String jti = UUID.randomUUID().toString();
        final Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        JWTEntity jwtEntity = new JWTEntity(
                jti,
                uid,
                LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault())
        );

        log.info("Saving new JWT.");
        return mongoJWTEntityRepository.save(jwtEntity)
                // this.invalidateAll()
                .then(Mono.fromCallable(() -> this.buildToken(customClaims, jti, uid, expiration)))
                .doOnSuccess(token -> log.info("JWT successfully issued."))
                .doOnError(error -> log.error("Failed to issue JWT: {}", error.getMessage()))
                .as(transactionalOperator::transactional);
    }

    private String buildToken(Map<String, String> customClaims, String jti, String uid, Date expiration) {
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
