package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Mono;

public interface VerificationTokenService {

    Mono<Void> issue(String identifier, Object carrierObject);

    Mono<VerificationToken> verify(String token);

    Mono<Void> performVerifiedTokenAction(VerificationToken verificationToken);

    Mono<VerificationToken> getVerificationToken(String id);

    Mono<Void> deleteVerificationToken(String id);
}