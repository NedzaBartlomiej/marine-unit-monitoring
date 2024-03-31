package pl.bartlomiej.marineunitmonitoring.user;

import org.bson.types.ObjectId;

public interface UserService {


    User getUser(ObjectId objectId);


    User createUser(User user);

    void deleteUser(ObjectId objectId);
}