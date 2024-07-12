package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MongoJWTEntityRepository extends ReactiveMongoRepository<JWTEntity, String> {
}
