package pl.bartlomiej.marineunitmonitoring.shiptrackhistory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory();

    Mono<Void> clearShipHistory(Long mmsi);
}
