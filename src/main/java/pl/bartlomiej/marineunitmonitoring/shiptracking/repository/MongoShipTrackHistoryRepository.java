package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Mono;

public interface MongoShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Mono<Void> deleteAllByMmsi(Long mmsi);
}
