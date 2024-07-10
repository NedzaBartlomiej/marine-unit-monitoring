package pl.bartlomiej.marineunitmonitoring.security.authentication.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface AuthenticationService {
    Mono<Map<String, String>> authenticate(String id, String email, String password);
}
