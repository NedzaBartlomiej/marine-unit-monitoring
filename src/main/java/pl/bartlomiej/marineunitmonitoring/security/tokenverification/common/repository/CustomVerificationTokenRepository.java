package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Flux;

public interface CustomVerificationTokenRepository {
    Flux<VerificationToken> findExpiredTokens();
}
