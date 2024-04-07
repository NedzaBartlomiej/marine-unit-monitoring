package pl.bartlomiej.marineunitmonitoring.user;

import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

public interface UserService {


    User getUser(String id);


    User createUser(User user);

    List<TrackedShip> getTrackedShips(String id);

    TrackedShip addTrackedShip(String id, Long mmsi);

    void removeTrackedShip(String id, Long mmsi);

    void deleteUser(String id);
}