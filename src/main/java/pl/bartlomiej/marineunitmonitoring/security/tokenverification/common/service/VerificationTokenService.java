package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Mono;

public interface VerificationTokenService<T extends VerificationToken, CarrierObject> {

    Mono<Void> issue(String identifier, CarrierObject carrierObject);

    Mono<T> verify(String token);

    Mono<Void> performVerifiedTokenAction(T verificationToken);

    Mono<T> getVerificationToken(String id);

    Mono<Void> deleteVerificationToken(String id);
}