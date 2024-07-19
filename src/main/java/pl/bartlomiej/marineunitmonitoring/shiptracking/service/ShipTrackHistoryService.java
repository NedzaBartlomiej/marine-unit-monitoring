package pl.bartlomiej.marineunitmonitoring.shiptracking.service;

import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory(String userId, LocalDateTime from, LocalDateTime to);

    Mono<Void> clearShipHistory(String mmsi);
}
