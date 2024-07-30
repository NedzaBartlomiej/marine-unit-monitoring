package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository;

import reactor.core.publisher.Mono;

public interface CustomJWTEntityRepository {
    Mono<Void> updateIsValid(String id, boolean isValid);

    Mono<Void> updateIsValidByUid(String uid, boolean isValid);
}
