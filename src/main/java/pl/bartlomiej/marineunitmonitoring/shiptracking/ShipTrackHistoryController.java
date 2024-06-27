package pl.bartlomiej.marineunitmonitoring.shiptracking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name())")
    @GetMapping
    public ResponseEntity<Flux<ServerSentEvent<ResponseModel<ShipTrack>>>> getShipTrackHistory(
            Principal principal,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {

        return ResponseEntity.ok(shipTrackHistoryService.getShipTrackHistory(principal.getName(), from, to)
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