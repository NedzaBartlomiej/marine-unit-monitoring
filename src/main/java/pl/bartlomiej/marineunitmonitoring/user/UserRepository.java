package pl.bartlomiej.marineunitmonitoring.user;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    Boolean existsByEmail(String email);
}