package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import java.util.List;

public interface TrackedShipService {
    List<TrackedShip> getTrackedShips(String id);

    List<TrackedShip> getTrackedShips();

    TrackedShip addTrackedShip(String id, Long mmsi);

    void removeTrackedShip(String id, Long mmsi);

    void removeTrackedShip(Long mmsi);
}
