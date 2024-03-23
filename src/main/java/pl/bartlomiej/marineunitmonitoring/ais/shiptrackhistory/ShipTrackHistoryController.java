package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping
    public ResponseEntity<Flux<ServerSentEvent<ResponseModel<ShipTrack>>>> getShipTrackHistory() {
        return ResponseEntity.ok(shipTrackHistoryService.getShipTrackHistory()
                .map(response ->
                        ServerSentEvent.<ResponseModel<ShipTrack>>builder()
                                .id(response.getId().toString())
                                .event("NEW_SHIP_TRACK_EVENT")
                                .data(
                                        ResponseModel.<ShipTrack>builder()
                                                .httpStatus(OK)
                                                .httpStatusCode(OK.value())
                                                .body(of("ShipTracks", response))
                                                .build()
                                )
                                .build()
                )
        );
    }


    // todo getTrackedShips()

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