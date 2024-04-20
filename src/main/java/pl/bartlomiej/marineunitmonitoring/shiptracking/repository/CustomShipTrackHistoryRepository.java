package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomShipTrackHistoryRepository {
    Flux<ShipTrack> findByMmsiInAndReadingTimeBetween(List<Long> mmsis, LocalDateTime from, LocalDateTime to);
}
