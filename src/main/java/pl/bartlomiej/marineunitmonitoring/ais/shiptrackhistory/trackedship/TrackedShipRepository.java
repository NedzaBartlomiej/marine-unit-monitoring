package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TrackedShipRepository extends ReactiveMongoRepository<TrackedShip, Long> {

    Mono<TrackedShip> findByMmsi(Long mmsi);
}
