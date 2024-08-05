package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service;

import io.jsonwebtoken.Claims;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Map;

public interface JWTService {

    Mono<Map<String, String>> createTokenPacket(String uid, String email);

    Mono<String> createAccessToken(String uid, String email);

    Mono<String> createRefreshToken(String uid, String email);

    Mono<Map<String, String>> refreshAccessToken(String refreshToken);

    Mono<Boolean> isValid(String token);

    Mono<Void> invalidate(String token);

    Mono<Void> invalidateAll(String uid);

    String extract(ServerWebExchange exchange);

    Claims extractClaims(String token);

    Key getSigningKey();
}