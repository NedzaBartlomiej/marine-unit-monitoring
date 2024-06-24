package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface MongoUserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);
}