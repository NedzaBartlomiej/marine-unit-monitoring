package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.common.error.InvalidTokenException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.CommonCustomTokenClaim.TYPE;
import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.JWTType.REFRESH_TOKEN;

@Slf4j
@Component
public class JWTTypeVerifier extends AbstractJWTVerifier implements WebFilter {

    private final List<String> refreshTokenPaths = List.of(
            "/authentication/refreshAccessToken",
            "/authentication/invalidateToken"
    );

    protected JWTTypeVerifier(JWTService jwtService) {
        super(jwtService);
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return super.filter(exchange, chain);
    }

    @Override
    protected Mono<Void> verifyToken(ServerWebExchange exchange, WebFilterChain chain, Claims claims) {
        return Mono.just(claims)
                .map(c -> c.get(TYPE.getClaim(), String.class))
                .flatMap(type -> {
                    if (type.equals(REFRESH_TOKEN.getType())) {
                        log.info("Invalid token type, refresh token.");
                        return Mono.error(InvalidTokenException::new);
                    }
                    return chain.filter(exchange);
                });
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
