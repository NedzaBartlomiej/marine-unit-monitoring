package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory();

    Mono<List<TrackedShip>> getTrackedShips();

    Mono<TrackedShip> saveTrackedShip(TrackedShip trackedShip);

    Mono<Void> deleteTrackedShip(Long mmsi);
}
