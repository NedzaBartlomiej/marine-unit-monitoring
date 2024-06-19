package pl.bartlomiej.marineunitmonitoring.security.webfilters;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.nested.Role;
import pl.bartlomiej.marineunitmonitoring.user.service.reactive.ReactiveUserService;
import pl.bartlomiej.marineunitmonitoring.user.service.sync.UserService;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static reactor.core.publisher.Mono.empty;

@Component
public class OAuth2JwtWebFilter implements WebFilter {

    private final ReactiveUserService reactiveUserService;

    public OAuth2JwtWebFilter(ReactiveUserService reactiveUserService) {
        this.reactiveUserService = reactiveUserService;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (this.shouldNotFilter(exchange)) {
            return chain.filter(exchange);
        }

        User oAuthNewUser = new User(jwtEmail, jwtUsername, Role.ROLE_SIGNED);

        return reactiveUserService.getUserByEmail(jwtEmail)
                .map(user -> reactiveUserService.updateUser(user.getId(), oAuthNewUser))
                .onErrorResume(reactiveUserService.createUser(oAuthNewUser))
                .then(chain.filter(exchange));
    }

    private boolean shouldNotFilter(final ServerWebExchange exchange) {

        Jwt jwt = this.extractJwtFromRequest(exchange);
        var authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);

        if (jwt == null) {
            return true;
        }
        if (authHeader == null) {
            return true;
        }
        if (!authHeader.startsWith("Bearer ")) {
            return true;
        }
        return false;
    }

    private Jwt extractJwtFromRequest(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof AbstractOAuth2TokenAuthenticationToken)
                .cast(AbstractOAuth2TokenAuthenticationToken.class)
                .filter(authToken -> authToken.getToken() instanceof Jwt)
                .map(authToken -> (Jwt) authToken.getToken())
                .switchIfEmpty(empty())
                .onErrorResume(e -> empty())
                .block();
    }
}
