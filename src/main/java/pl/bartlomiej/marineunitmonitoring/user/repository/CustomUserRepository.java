package pl.bartlomiej.marineunitmonitoring.user.repository;

import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;

import java.util.List;

public interface CustomUserRepository {

    TrackedShip pushTrackedShip(String id, TrackedShip trackedShip);

    void pullTrackedShip(String id, Long mmsi);

    void pullTrackedShip(Long mmsi);

    List<TrackedShip> getTrackedShips(String id);

    List<TrackedShip> getTrackedShips();
}
