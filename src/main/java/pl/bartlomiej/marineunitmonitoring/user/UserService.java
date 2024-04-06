package pl.bartlomiej.marineunitmonitoring.user;

import org.bson.types.ObjectId;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

public interface UserService {


    User getUser(String id);


    User createUser(User user);

    TrackedShip addTrackedShip(String id, Long mmsi);

    void deleteUser(String id);
}