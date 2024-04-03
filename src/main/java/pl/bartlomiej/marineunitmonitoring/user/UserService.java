package pl.bartlomiej.marineunitmonitoring.user;

import org.bson.types.ObjectId;

public interface UserService {


    User getUser(String id);


    User createUser(User user);

    void deleteUser(String id);
}