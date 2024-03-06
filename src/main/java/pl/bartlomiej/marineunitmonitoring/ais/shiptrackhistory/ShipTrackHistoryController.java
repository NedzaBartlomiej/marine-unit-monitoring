package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping
    public Mono<ResponseEntity<List<ShipTrack>>> getShipTrackHistory() {
        return shipTrackHistoryService.getShipTrackHistory()
                .map(response ->
                        ResponseEntity
                                .status(OK)
                                .body(response));
    }

    @PostMapping(value = "/tracked-ships")
    public Mono<Void> saveTrackedShip(@RequestBody @Valid TrackedShip trackedShip) {
        return shipTrackHistoryService.saveTrackedShip(trackedShip);
    }

    @DeleteMapping(value = "/tracked-ships/{mmsi}")
    public Mono<Void> deleteTrackedShip(@PathVariable Long mmsi) {
        return shipTrackHistoryService.deleteTrackedShip(mmsi);
    }
}