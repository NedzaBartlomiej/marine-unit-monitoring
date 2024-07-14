package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomShipTrackHistoryRepository {
    Flux<ShipTrack> findByMmsiInAndReadingTimeBetween(List<String> mmsis, LocalDateTime from, LocalDateTime to);

    Mono<ShipTrack> getLatest(String mmsi);
}
