package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.resetpassword.service;

import reactor.core.publisher.Mono;

public interface ResetPasswordService {
    Mono<Void> initiatePasswordResettingFlow(String email);
}
