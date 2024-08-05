package pl.bartlomiej.marineunitmonitoring.security.tokenverification.twofactorauth;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import reactor.core.publisher.Mono;

public interface MongoTwoFactorAuthVerificationTokenRepository extends MongoVerificationTokenRepository<TwoFactorAuthVerificationToken> {
    Mono<TwoFactorAuthVerificationToken> findByCode(String code);
}
