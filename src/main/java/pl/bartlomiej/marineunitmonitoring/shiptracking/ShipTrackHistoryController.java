package pl.bartlomiej.marineunitmonitoring.shiptracking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.time.LocalDateTime;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/v1/ship-track-history")
public class ShipTrackHistoryController {

    private final ShipTrackHistoryService shipTrackHistoryService;
    private final UserService userService;

    public ShipTrackHistoryController(ShipTrackHistoryService shipTrackHistoryService, UserService userService) {
        this.shipTrackHistoryService = shipTrackHistoryService;
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole(" +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name()," +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name()" +
            ")"
    )
    @GetMapping
    public Flux<ServerSentEvent<ResponseModel<ShipTrack>>> getShipTrackHistory(
            Principal principal,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {

        return userService.identifyUser(principal.getName())
                .flatMapMany(id -> shipTrackHistoryService.getShipTrackHistory(id, from, to)
                        .map(response ->
                                ServerSentEvent.<ResponseModel<ShipTrack>>builder()
                                        .id(response.getId())
                                        .event("NEW_SHIP_TRACK_EVENT")
                                        .data(
                                                ResponseModel.<ShipTrack>builder()
                                                        .httpStatus(OK)
                                                        .httpStatusCode(OK.value())
                                                        .body(of("shipTracks", response))
                                                        .build()
                                        )
                                        .build()
                        )
                );
    }
}