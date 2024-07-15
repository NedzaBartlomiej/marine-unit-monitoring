package pl.bartlomiej.marineunitmonitoring.security.emailverification.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.EmailVerificationEntity;

public interface MongoEmailVerificationEntityRepository extends ReactiveMongoRepository<EmailVerificationEntity, String> {
}
