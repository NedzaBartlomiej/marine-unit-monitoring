package pl.bartlomiej.marineunitmonitoring.shiptracking;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Mono<Void> deleteAllByMmsi(Long mmsi);
}
