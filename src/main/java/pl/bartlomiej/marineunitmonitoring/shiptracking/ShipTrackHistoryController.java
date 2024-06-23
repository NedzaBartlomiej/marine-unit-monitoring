package pl.bartlomiej.marineunitmonitoring.shiptracking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping
    public ResponseEntity<Flux<ServerSentEvent<ResponseModel<ShipTrack>>>> getShipTrackHistory(
            @RequestBody List<Long> mmsis,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {

        // todo remove mmsis from body, get mmsis from authenticated user
        return ResponseEntity.ok(shipTrackHistoryService.getShipTrackHistory(mmsis, from, to)
                .map(response ->
                        ServerSentEvent.<ResponseModel<ShipTrack>>builder()
                                .id(response.getId())
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
}