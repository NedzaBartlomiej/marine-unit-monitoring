package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Mono;

public interface MongoShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, String> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Mono<Void> deleteByMmsi(Long mmsi);

    Mono<Boolean> existsByMmsi(Long mmsi);
}
