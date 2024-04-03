package pl.bartlomiej.marineunitmonitoring.shiptrackhistory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Mono<Void> deleteShipTracksByMmsi(Long mmsi);
}
