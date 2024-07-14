package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import io.jsonwebtoken.Claims;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Map;

public interface JWTService {

    String createAccessToken(String uid, String email);

    String createRefreshToken(String uid, String email);

    Mono<Map<String, String>> refreshAccessToken(String refreshToken);

    Mono<Void> invalidate(String token);

    Mono<Boolean> isBlacklisted(String jti);

    String extract(ServerWebExchange exchange);

    Claims extractClaims(String token);

    Key getSigningKey();
}
