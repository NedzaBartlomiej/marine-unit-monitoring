package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;

public interface MongoJWTEntityRepository extends ReactiveMongoRepository<JWTEntity, String> {
}
