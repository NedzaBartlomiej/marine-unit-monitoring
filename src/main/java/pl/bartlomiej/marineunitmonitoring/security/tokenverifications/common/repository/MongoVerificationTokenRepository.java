package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

public interface MongoVerificationTokenRepository extends ReactiveMongoRepository<VerificationToken, String> {
}
