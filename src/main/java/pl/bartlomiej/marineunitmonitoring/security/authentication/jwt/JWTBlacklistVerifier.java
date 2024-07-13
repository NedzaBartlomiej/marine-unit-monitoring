package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.common.error.InvalidTokenException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

@Component
public class JWTBlacklistVerifier implements WebFilter {

    private static final String BEARER_REGEX = "^Bearer\\s+(\\S+)";
    private static final Logger log = LoggerFactory.getLogger(JWTBlacklistVerifier.class);
    private final JWTService jwtService;
    private final ServerAuthenticationFailureHandler serverAuthenticationFailureHandler;

    public JWTBlacklistVerifier(JWTService jwtService, ResponseModelServerAuthenticationEntryPoint serverAuthenticationEntryPoint) {
        this.jwtService = jwtService;
        this.serverAuthenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint);
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

        log.info("Validating JWT...");
        return jwtService.isBlacklisted(jti)
                .flatMap(isBlacklisted -> isBlacklisted
                        ? Mono.error(InvalidTokenException::new)
                        : chain.filter(exchange)
                ).onErrorResume(InvalidTokenException.class, ex -> // todo look for places where it can be implemented
                        serverAuthenticationFailureHandler.onAuthenticationFailure(
                                new WebFilterExchange(exchange, chain), ex)
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
