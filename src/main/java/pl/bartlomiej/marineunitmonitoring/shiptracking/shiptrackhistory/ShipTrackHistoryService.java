package pl.bartlomiej.marineunitmonitoring.shiptracking.shiptrackhistory;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis);
}
