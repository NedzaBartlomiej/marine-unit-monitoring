package pl.bartlomiej.marineunitmonitoring.user.repository.sync;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<User, String> {

    boolean existsByEmail(String email);

    Optional<User> findByOpenId(String openId);
}
