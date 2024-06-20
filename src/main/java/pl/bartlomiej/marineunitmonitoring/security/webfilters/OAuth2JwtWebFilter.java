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

import static reactor.core.publisher.Mono.empty;

@Component
@Slf4j
public class OAuth2JwtWebFilter implements WebFilter {

    private final ReactiveUserService reactiveUserService;

    public OAuth2JwtWebFilter(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.extractJwtFromRequest(exchange)
                .map(jwt -> {
                    log.info("Jwt received: {}", jwt.getTokenValue()); // todo trzeba wypisac ten token i na jwt.io sobie zobaczyc nazwy claimow i dzialac.
                    return jwt;
                })
                .then(chain.filter(exchange));
    }

    private boolean shouldNotFilter(final ServerWebExchange exchange) {
        return false;
    }

    private Mono<Jwt> extractJwtFromRequest(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof AbstractOAuth2TokenAuthenticationToken)
                .cast(AbstractOAuth2TokenAuthenticationToken.class)
                .filter(authToken -> authToken.getToken() instanceof Jwt)
                .map(authToken -> (Jwt) authToken.getToken())
                .switchIfEmpty(empty())
                .onErrorResume(e -> empty());
    }
}