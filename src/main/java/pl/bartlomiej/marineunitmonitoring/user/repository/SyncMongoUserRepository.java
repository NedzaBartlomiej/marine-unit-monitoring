package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

public interface SyncMongoUserRepository extends MongoRepository<User, String> {
}
