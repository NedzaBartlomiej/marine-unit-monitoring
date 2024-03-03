package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<Void> deleteShipTracksByMmsi(Long mmsi);
}
