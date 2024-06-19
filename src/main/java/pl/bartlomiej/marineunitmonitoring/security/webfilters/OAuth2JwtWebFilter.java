package pl.bartlomiej.marineunitmonitoring.security.webfilters;

import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class OAuth2JwtWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        return chain.filter(exchange);
    }

    private boolean shouldNotFilter(final ServerWebExchange exchange) {

        String bearerPref = "Bearer ";
        var authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
    }
}
