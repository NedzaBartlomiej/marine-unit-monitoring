package pl.bartlomiej.marineunitmonitoring.security.authentication.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection.service.TrustedIpAddressService;
import reactor.core.publisher.Mono;

import java.util.Map;

import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.JWTType.ACCESS_TOKEN;
import static pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl.JWTType.REFRESH_TOKEN;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final TrustedIpAddressService trustedIpAddressService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthenticationServiceImpl(TrustedIpAddressService trustedIpAddressService, @Qualifier("userDetailsReactiveAuthenticationManager") ReactiveAuthenticationManager authenticationManager, JWTService jwtService) {
        this.trustedIpAddressService = trustedIpAddressService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Map<String, String>> authenticate(String id, String email, String password, String ipAddress) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(id, password);
        return authenticationManager.authenticate(authenticationToken)
                .flatMap(ignoredAuthentication -> trustedIpAddressService.processTrustedIpProtection(id, ipAddress))
                .then(Mono.just(
                        Map.of(
                                REFRESH_TOKEN.getType(), jwtService.createRefreshToken(id, email),
                                ACCESS_TOKEN.getType(), jwtService.createAccessToken(id, email))
                ));
    }
}