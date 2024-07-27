package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomVerificationTokenRepository {
    Flux<VerificationToken> findExpiredTokens();

    Mono<Void> updateIsVerified(String id, boolean isVerified);
}
