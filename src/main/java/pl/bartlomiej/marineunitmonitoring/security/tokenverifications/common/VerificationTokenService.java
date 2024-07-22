package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common;

import reactor.core.publisher.Mono;

public interface VerificationTokenService<VR, I> {
    Mono<Void> issue(I identifier);

    Mono<VR> verify(String token);
}
