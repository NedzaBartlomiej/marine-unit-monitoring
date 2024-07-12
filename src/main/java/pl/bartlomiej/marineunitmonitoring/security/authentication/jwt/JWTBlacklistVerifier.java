package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.common.error.InvalidTokenException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import reactor.core.publisher.Mono;

@Component
public class JWTBlacklistVerifier implements WebFilter {

    private static final String BEARER_REGEX = "^Bearer\\s+(\\S+)";
    private static final Logger log = LoggerFactory.getLogger(JWTBlacklistVerifier.class);
    private final JWTService jwtService;

    public JWTBlacklistVerifier(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        if (this.shouldNotFilter(exchange)) {
            return chain.filter(exchange);
        }

        String jwt = jwtService.extract(exchange);
        String jti;
        try {
            log.info("Attempting to validate JWT...");
            jti = jwtService.getJti(jwt);
        } catch (JwtException e) {
            log.error("Some JWT problem occurred while extracting JWT {}, forwarding to authentication flow.", e.getMessage());
            return chain.filter(exchange);
        }

        // todo handle in security handler
        // todo exception should thrown, but it is not throwing
        log.info("Checking JWTs blacklist...");
        return jwtService.isBlacklisted(jti)
                .flatMap(isBlacklisted -> isBlacklisted
                        ? Mono.error(InvalidTokenException::new)
                        : chain.filter(exchange)
                );
    }

    private boolean shouldNotFilter(@NonNull ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            log.error("Authorization header doesn't exist, or is empty.");
            return true;
        }
        if (!authHeader.matches(BEARER_REGEX)) {
            log.error("Authorization header is not matching with bearer token requirements.");
            return true;
        }
        return false;
    }
}
