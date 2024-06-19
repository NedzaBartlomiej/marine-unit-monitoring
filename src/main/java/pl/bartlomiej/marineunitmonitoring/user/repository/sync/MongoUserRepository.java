package pl.bartlomiej.marineunitmonitoring.user.repository.sync;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

public interface MongoUserRepository extends MongoRepository<User, String> {
}
