package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping
    public Mono<List<ShipTrack>> getShipTrackHistory() {
        return shipTrackHistoryService.getShipTrackHistory();
    }

    @PostMapping(value = "/tracked-ships")
    public Mono<Void> saveTrackedShip(@RequestBody TrackedShip trackedShip) {
        return shipTrackHistoryService.saveTrackedShip(trackedShip);
    }

    @DeleteMapping(value = "/tracked-ships/{mmsi}")
    public Mono<Void> deleteTrackedShip(@PathVariable Long mmsi) {
        return shipTrackHistoryService.deleteTrackedShip(mmsi);
    }
}
