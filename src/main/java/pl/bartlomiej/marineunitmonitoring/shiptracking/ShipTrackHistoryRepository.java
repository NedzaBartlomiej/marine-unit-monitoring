package pl.bartlomiej.marineunitmonitoring.shiptracking;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Flux<ShipTrack> findByReadingTimeBetween(LocalDateTime from, LocalDateTime to);

    Mono<Void> deleteAllByMmsi(Long mmsi);
}
