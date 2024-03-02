package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ShipTrackHistoryService {
    Mono<List<ShipTrack>> getShipTrackHistory();

    Mono<Void> saveTrackedShip(TrackedShip trackedShip);

    Mono<Void> deleteTrackedShip(Long mmsi);
}
