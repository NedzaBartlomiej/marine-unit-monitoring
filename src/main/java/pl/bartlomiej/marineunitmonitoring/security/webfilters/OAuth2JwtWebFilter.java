package pl.bartlomiej.marineunitmonitoring.security.webfilters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.reactive.ReactiveMongoUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.reactive.ReactiveUserService;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import static java.util.List.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.ROLE_SIGNED;
import static reactor.core.publisher.Mono.error;

@Component
@Slf4j
public class OAuth2JwtWebFilter implements WebFilter {

    private final ReactiveUserService reactiveUserService;
    private final ReactiveMongoUserRepository reactiveMongoUserRepository;

    public OAuth2JwtWebFilter(ReactiveUserService reactiveUserService, ReactiveMongoUserRepository reactiveMongoUserRepository) {
        this.reactiveUserService = reactiveUserService;
        this.reactiveMongoUserRepository = reactiveMongoUserRepository;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        if (this.shouldNotFilter(exchange)) return chain.filter(exchange);

        return this.extractJwtFromRequest(exchange)
                .flatMap(jwt -> {

                    String openId = jwt.getSubject();
                    String email = jwt.getClaimAsString("email");
                    String username = jwt.getClaimAsString("name");

                    return reactiveUserService.getUserByOpenId(openId)
                            .flatMap(user -> {
                                user.setEmail(email);
                                user.setUsername(username);
                                return reactiveMongoUserRepository.save(user);
                            })
                            .switchIfEmpty(reactiveUserService.createUser(
                                    new User(openId, username, email, of(ROLE_SIGNED)))
                            );

                }).then(chain.filter(exchange));
    }

    private boolean shouldNotFilter(final ServerWebExchange exchange) {
        // shouldNotFilter when Google is not issuer
        return
                exchange.getRequest().getHeaders().get(AUTHORIZATION) == null;
    }

    private Mono<Jwt> extractJwtFromRequest(final ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(AbstractOAuth2TokenAuthenticationToken.class)
                .filter(authToken -> authToken.getToken() instanceof Jwt)
                .map(authToken -> (Jwt) authToken.getToken())
                .switchIfEmpty(error(new Throwable("Invalid token. JWT is required type.")));
    }
}