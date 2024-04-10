package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import java.util.List;

public interface TrackedShipService {
    List<TrackedShip> getTrackedShips(String id);

    TrackedShip addTrackedShip(String id, Long mmsi);

    void removeTrackedShip(String id, Long mmsi);

    void removeTrackedShip(Long mmsi);
}
