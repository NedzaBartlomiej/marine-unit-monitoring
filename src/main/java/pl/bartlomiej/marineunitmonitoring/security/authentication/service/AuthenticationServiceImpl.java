package pl.bartlomiej.marineunitmonitoring.security.authentication.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthenticationServiceImpl(@Qualifier("userDetailsReactiveAuthenticationManager") ReactiveAuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Map<String, String>> authenticate(String id, String email, String password) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(id, password);
        return authenticationManager.authenticate(authenticationToken)
                .map(ignoredAuthentication -> Map.of(
                        "refreshToken", jwtService.createRefreshToken(id, email),
                        "accessToken", jwtService.createAccessToken(id, email)
                ));
    }
}