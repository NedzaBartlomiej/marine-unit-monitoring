package pl.bartlomiej.marineunitmonitoring.security.authentication.service;

import pl.bartlomiej.marineunitmonitoring.security.authentication.AuthResponse;
import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<AuthResponse> authenticate(User user, String authPassword, String ipAddress);
}
