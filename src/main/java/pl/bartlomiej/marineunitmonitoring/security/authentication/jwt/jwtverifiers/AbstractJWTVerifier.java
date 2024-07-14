package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractJWTVerifier {

    private static final String BEARER_REGEX = "^Bearer\\s+(\\S+)";
    private final JWTService jwtService;

    protected AbstractJWTVerifier(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    protected Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (this.shouldNotFilter(exchange)) {
            return chain.filter(exchange);
        }

        Claims claims;
        try {
            claims = this.validateToken(exchange);
        } catch (JwtException e) {
            return chain.filter(exchange);
        }

        return this.verifyToken(exchange, chain, claims);
    }

    protected Claims validateToken(ServerWebExchange exchange) throws JwtException {
        String token = jwtService.extract(exchange);
        return jwtService.extractClaims(token);
    }

    protected abstract Mono<Void> verifyToken(ServerWebExchange exchange, WebFilterChain chain, Claims claims);

    protected boolean shouldNotFilter(ServerWebExchange exchange) {
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
