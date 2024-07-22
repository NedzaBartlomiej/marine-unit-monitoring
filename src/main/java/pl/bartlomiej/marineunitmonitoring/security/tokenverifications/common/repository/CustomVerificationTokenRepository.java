package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import reactor.core.publisher.Flux;

public interface CustomVerificationTokenRepository {
    Flux<VerificationToken> findExpiredTokens();
}
