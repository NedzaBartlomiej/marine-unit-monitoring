package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping
    public Mono<ResponseEntity<ResponseModel<List<ShipTrack>>>> getShipTrackHistory() {
        return shipTrackHistoryService.getShipTrackHistory()
                .map(response ->
                        ResponseEntity.ok(
                                ResponseModel.<List<ShipTrack>>builder()
                                        .httpStatus(OK)
                                        .httpStatusCode(OK.value())
                                        .body(of("ShipTracks", response))
                                        .build()
                        )
                );
    }

    @PostMapping(value = "/tracked-ships")
    public Mono<ResponseEntity<ResponseModel<TrackedShip>>> saveTrackedShip(@RequestBody @Valid TrackedShip trackedShip) {
        return shipTrackHistoryService.saveTrackedShip(trackedShip)
                .map(response ->
                        ResponseEntity
                                .status(CREATED)
                                .body(
                                        ResponseModel.<TrackedShip>builder()
                                                .httpStatus(CREATED)
                                                .httpStatusCode(CREATED.value())
                                                .body(of("TrackedShip", response))
                                                .build()
                                )
                );
    }

    @DeleteMapping(value = "/tracked-ships/{mmsi}")
    public Mono<ResponseEntity<ResponseModel<Void>>> deleteTrackedShip(@PathVariable Long mmsi) {
        return shipTrackHistoryService.deleteTrackedShip(mmsi)
                .then(Mono.just(
                        ResponseEntity.ok(
                                ResponseModel.<Void>builder()
                                        .httpStatus(OK)
                                        .httpStatusCode(OK.value())
                                        .message("Ship has been deleted from tracking list.")
                                        .build()
                        ))
                );
    }
}