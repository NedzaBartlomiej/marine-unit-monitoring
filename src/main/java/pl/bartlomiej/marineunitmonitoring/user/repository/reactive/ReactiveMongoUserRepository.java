package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

public interface MongoUserRepository extends ReactiveMongoRepository<User, String> {
}