package pl.bartlomiej.marineunitmonitoring.shiptracking;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {

    Mono<ShipTrack> findByMmsi(Long mmsi);

    Flux<ShipTrack> findByReadingTimeBetweenAndMmsiIsIn(
            LocalDateTime from, LocalDateTime to, List<Long> mmsis);

    Mono<Void> deleteAllByMmsi(Long mmsi);
}
