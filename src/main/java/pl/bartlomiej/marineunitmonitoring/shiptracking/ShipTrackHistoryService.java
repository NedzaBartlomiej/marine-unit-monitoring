package pl.bartlomiej.marineunitmonitoring.shiptracking;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface ShipTrackHistoryService {
    Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to);
}
