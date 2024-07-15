package pl.bartlomiej.marineunitmonitoring.security.emailverification;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MongoEmailVerificationEntityRepository extends ReactiveMongoRepository<EmailVerificationEntity, String> {
}
