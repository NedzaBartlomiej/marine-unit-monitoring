package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import reactor.core.publisher.Mono;

public interface MongoVerificationTokenRepository<T extends VerificationToken> extends ReactiveMongoRepository<T, String> {
    Mono<Void> deleteByUid(String uid);
}
