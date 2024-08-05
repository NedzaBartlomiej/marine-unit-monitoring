package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Mono;

public interface VerificationTokenService<T extends VerificationToken, CarrierObject, VerifiedActionObject> {

    Mono<Void> issue(String uid, CarrierObject carrierObject);

    Mono<T> verify(String token);

    Mono<VerifiedActionObject> performVerifiedTokenAction(T verificationToken);

    Mono<T> getVerificationToken(String id);

    Mono<Void> deleteVerificationToken(String id);
}