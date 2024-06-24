package pl.bartlomiej.marineunitmonitoring.user.repository;

import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CustomUserRepository {

    Mono<TrackedShip> pushTrackedShip(String id, TrackedShip trackedShip);

    Mono<Void> pullTrackedShip(String id, Long mmsi);

    Mono<Void> pullTrackedShip(Long mmsi);

    Flux<TrackedShip> getTrackedShips(String id);

    Flux<TrackedShip> getTrackedShips();
}
