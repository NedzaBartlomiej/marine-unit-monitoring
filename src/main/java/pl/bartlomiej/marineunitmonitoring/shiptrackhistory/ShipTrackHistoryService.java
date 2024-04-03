package pl.bartlomiej.marineunitmonitoring.shiptrackhistory;

import reactor.core.publisher.Flux;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory();
}
