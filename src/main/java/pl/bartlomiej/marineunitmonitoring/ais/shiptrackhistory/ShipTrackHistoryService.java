package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory();

    Mono<TrackedShip> saveTrackedShip(TrackedShip trackedShip);

    Mono<Void> deleteTrackedShip(Long mmsi);
}
