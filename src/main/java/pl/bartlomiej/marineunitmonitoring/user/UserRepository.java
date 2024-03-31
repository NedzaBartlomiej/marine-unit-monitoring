package pl.bartlomiej.marineunitmonitoring.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    Boolean existsByEmail(String email);

}
