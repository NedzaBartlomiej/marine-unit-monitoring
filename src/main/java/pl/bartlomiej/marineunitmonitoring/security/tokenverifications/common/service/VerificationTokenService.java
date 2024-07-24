package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import reactor.core.publisher.Mono;

public interface VerificationTokenService {
    Mono<Void> issue(String userIdentifier);

    Mono<Void> verify(String token);

    Mono<VerificationToken> getVerificationToken(String id);

    Mono<Void> deleteVerificationToken(String id);
}