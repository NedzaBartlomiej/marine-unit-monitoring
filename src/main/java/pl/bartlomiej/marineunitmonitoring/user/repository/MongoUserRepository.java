package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;

public interface MongoUserRepository extends MongoRepository<User, String> {

    Boolean existsByEmail(String email);
}