package pl.bartlomiej.marineunitmonitoring.shiptrackhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import reactor.core.publisher.Flux;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @GetMapping //todo mmsis
    public ResponseEntity<Flux<ServerSentEvent<ResponseModel<ShipTrack>>>> getShipTrackHistory(Long[] mmsis) {
        return ResponseEntity.ok(shipTrackHistoryService.getShipTrackHistory()
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