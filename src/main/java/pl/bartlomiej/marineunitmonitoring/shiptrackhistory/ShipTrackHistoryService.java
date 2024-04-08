package pl.bartlomiej.marineunitmonitoring.shiptrackhistory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis);

    Mono<Void> clearShipHistory(Long mmsi);
}
