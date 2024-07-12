package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

public interface JWTService {

    String createAccessToken(String uid, String email);

    String createRefreshToken(String uid, String email);

    Mono<Void> invalidate(String token);

    Mono<Boolean> isBlacklisted(String jti);

    String extract(ServerWebExchange exchange);

    String getJti(String token);

    Key getSigningKey();
}
