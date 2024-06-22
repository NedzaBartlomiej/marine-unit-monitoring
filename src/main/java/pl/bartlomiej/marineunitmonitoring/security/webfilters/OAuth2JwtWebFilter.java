package pl.bartlomiej.marineunitmonitoring.security.webfilters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.user.service.reactive.ReactiveUserService;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static reactor.core.publisher.Mono.error;

@Component
@Slf4j
public class OAuth2JwtWebFilter implements WebFilter {

    private final ReactiveUserService reactiveUserService;

    public OAuth2JwtWebFilter(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (this.shouldNotFilter(exchange)) return chain.filter(exchange);

        // logic
        return this.extractJwtFromRequest(exchange)
                .map(jwt -> {
                    log.info("Jwt received: {}", jwt.getTokenValue());
                    return jwt;
                })
                .then(chain.filter(exchange));
    }

    private boolean shouldNotFilter(final ServerWebExchange exchange) {
        return
                exchange.getRequest().getHeaders().get(AUTHORIZATION) == null;
    }

    private Mono<Jwt> extractJwtFromRequest(final ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(AbstractOAuth2TokenAuthenticationToken.class)
                .filter(authToken -> authToken.getToken() instanceof Jwt)
                .map(authToken -> (Jwt) authToken.getToken())
                .switchIfEmpty(error(new Throwable("Invalid token. JWT is required type."))); // handle
    }
}