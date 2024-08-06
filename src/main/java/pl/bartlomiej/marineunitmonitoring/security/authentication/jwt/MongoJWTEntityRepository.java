package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoJWTEntityRepository extends ReactiveMongoRepository<JWTEntity, String> {
    Mono<Void> deleteAllByUid(String uid);
}
