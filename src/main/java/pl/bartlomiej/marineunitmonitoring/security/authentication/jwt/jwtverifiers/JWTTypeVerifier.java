package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.InvalidJWTException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.refreshtokenendpoint.RefreshTokenEndpointsProvider;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JWTTypeVerifier extends AbstractJWTVerifier implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTTypeVerifier.class);
    private final ServerAuthenticationFailureHandler serverAuthenticationFailureHandler; // todo maybe some refactor this list
    private final List<String> refreshTokenPaths;

    public JWTTypeVerifier(JWTService jwtService,
                           @Qualifier("responseModelServerAuthenticationEntryPoint") ServerAuthenticationEntryPoint serverAuthenticationEntryPoint,
                           RefreshTokenEndpointsProvider refreshTokenEndpointsProvider) {
        super(jwtService);
        this.serverAuthenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint);
        this.refreshTokenPaths = refreshTokenEndpointsProvider.getRefreshTokenPaths();
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return super.filter(exchange, chain, this.shouldNotFilter(exchange));
    }

    @Override
    protected Mono<Void> verifyToken(ServerWebExchange exchange, WebFilterChain chain, Claims claims) {
        log.info("Verifying JWT.");
        return Mono.just(claims)
                .map(c -> c.get(JWTConstants.TYPE_CLAIM, String.class))
                .flatMap(type -> {
                    if (type.equals(JWTConstants.REFRESH_TOKEN_TYPE)) {
                        log.info("Invalid JWT.");
                        return Mono.error(new InvalidJWTException());
                    }
                    log.info("Valid JWT, forwarding to further flow.");
                    return chain.filter(exchange);
                })
                .onErrorResume(InvalidJWTException.class, ex ->
                        serverAuthenticationFailureHandler.onAuthenticationFailure(
                                new WebFilterExchange(exchange, chain), ex)
                );
    }

    @Override
    protected boolean shouldNotFilter(ServerWebExchange exchange) {
        if (refreshTokenPaths.contains(exchange.getRequest().getPath().pathWithinApplication().value())) {
            log.info("Refresh access token request, forwarding to further flow..");
            return true;
        }
        return super.shouldNotFilter(exchange);
    }
}
