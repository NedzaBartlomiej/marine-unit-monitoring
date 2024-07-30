package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTTypeVerifier extends AbstractJWTVerifier implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTTypeVerifier.class);
    private final ServerAuthenticationFailureHandler serverAuthenticationFailureHandler;
    private final List<String> refreshTokenPaths = List.of(
            "/authentication/refresh-access-token",
            "/authentication/invalidate-token"
    );

    public JWTTypeVerifier(JWTService jwtService,
                           ResponseModelServerAuthenticationEntryPoint serverAuthenticationEntryPoint,
                           @Value("${project-properties.security.token.bearer.regex}") String bearerRegex) {
        super(jwtService, bearerRegex);
        this.serverAuthenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint);
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
                        return Mono.error(new InvalidBearerTokenException("Invalid JWT."));
                    }
                    log.info("Valid JWT, forwarding to further flow.");
                    return chain.filter(exchange);
                })
                .onErrorResume(InvalidBearerTokenException.class, ex ->
                        serverAuthenticationFailureHandler.onAuthenticationFailure(
                                new WebFilterExchange(exchange, chain), ex)
                );
    }

    @Override
    protected boolean shouldNotFilter(ServerWebExchange exchange) {
        String uriWithoutVersion = this.cutVersionFromUrl(
                exchange.getRequest().getPath().pathWithinApplication()
        );
        if (refreshTokenPaths.contains(uriWithoutVersion)) {
            log.info("Refresh access token request, forwarding to further flow..");
            return true;
        }
        return super.shouldNotFilter(exchange);
    }

    private String cutVersionFromUrl(PathContainer pathContainer) {
        return pathContainer.elements().stream()
                .skip(2)
                .map(PathContainer.Element::value)
                .collect(Collectors.joining());
    }
}
