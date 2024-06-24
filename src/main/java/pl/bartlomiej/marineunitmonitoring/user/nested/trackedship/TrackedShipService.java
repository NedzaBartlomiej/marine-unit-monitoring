package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrackedShipService {
    Flux<TrackedShip> getTrackedShips(String id);

    Mono<TrackedShip> addTrackedShip(String id, Long mmsi);

    Mono<Void> removeTrackedShip(String id, Long mmsi);

    Mono<Void> removeTrackedShip(Long mmsi);
}
