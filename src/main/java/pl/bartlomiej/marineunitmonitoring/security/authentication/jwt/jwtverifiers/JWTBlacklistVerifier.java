package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.common.error.authexceptions.InvalidTokenException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JWTBlacklistVerifier extends AbstractJWTVerifier implements WebFilter {


    private final JWTService jwtService;
    private final ServerAuthenticationFailureHandler serverAuthenticationFailureHandler;

    public JWTBlacklistVerifier(JWTService jwtService, ResponseModelServerAuthenticationEntryPoint serverAuthenticationEntryPoint) {
        super(jwtService);
        this.jwtService = jwtService;
        this.serverAuthenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint);
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return super.filter(exchange, chain);
    }


    @Override
    protected Mono<Void> verifyToken(ServerWebExchange exchange, WebFilterChain chain, Claims claims) {
        return jwtService.isBlacklisted(claims.getId())
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.error("Invalid JWT.");
                        return Mono.error(InvalidTokenException::new);
                    }
                    log.info("Valid JWT, forwarding to further flow.");
                    return chain.filter(exchange);
                })
                .onErrorResume(InvalidTokenException.class, ex ->
                        serverAuthenticationFailureHandler.onAuthenticationFailure(
                                new WebFilterExchange(exchange, chain), ex)
                );
    }
}
