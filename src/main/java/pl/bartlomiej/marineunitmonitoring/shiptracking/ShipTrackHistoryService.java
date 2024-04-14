package pl.bartlomiej.marineunitmonitoring.shiptracking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to);

    Mono<Void> clearShipHistory(Long mmsi);
}
