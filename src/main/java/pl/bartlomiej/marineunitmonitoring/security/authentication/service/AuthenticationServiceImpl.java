package pl.bartlomiej.marineunitmonitoring.security.authentication.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.security.authentication.AuthResponse;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.service.IpAuthProtectionService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth.service.TwoFactorAuthService;
import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final IpAuthProtectionService ipAuthProtectionService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;

    public AuthenticationServiceImpl(IpAuthProtectionService ipAuthProtectionService,
                                     @Qualifier("userDetailsReactiveAuthenticationManager") ReactiveAuthenticationManager authenticationManager,
                                     JWTService jwtService, TwoFactorAuthService twoFactorAuthService) {
        this.ipAuthProtectionService = ipAuthProtectionService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public Mono<AuthResponse> authenticate(User user, String passwordCredential, String ipAddress) {
        return this.authenticateUser(user, passwordCredential)
                .flatMap(ignored -> ipAuthProtectionService.processProtection(user.getId(), ipAddress))
                .then(this.processAuthentication(user));
    }

    private Mono<Authentication> authenticateUser(User user, String passwordCredential) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(user.getId(), passwordCredential);
        return authenticationManager.authenticate(authenticationToken);
    }

    private Mono<AuthResponse> processAuthentication(User user) {
        if (user.getTwoFactorAuthEnabled()) {
            return this.handleTwoFactorAuthentication(user);
        } else {
            return this.createAuthResponse(user);
        }
    }

    private Mono<AuthResponse> handleTwoFactorAuthentication(User user) {
        return twoFactorAuthService.issue(user.getId(), null)
                .then(Mono.just(new AuthResponse("2FA_AUTH_ENABLED,CODE_SENT", null)));
    }

    private Mono<AuthResponse> createAuthResponse(User user) {
        return jwtService.createTokenPacket(user.getId(), user.getEmail())
                .map(tokens -> new AuthResponse("AUTHENTICATED", tokens));
    }
}