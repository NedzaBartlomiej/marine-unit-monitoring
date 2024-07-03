package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

import java.util.Optional;

public interface SyncMongoUserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
