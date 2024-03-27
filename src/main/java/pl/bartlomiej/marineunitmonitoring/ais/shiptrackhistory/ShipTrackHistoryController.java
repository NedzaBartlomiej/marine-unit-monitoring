package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto.TrackedShipDtoMapper;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto.TrackedShipReadDto;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto.TrackedShipSaveDto;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import reactor.core.publisher.Flux;
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
    private final TrackedShipDtoMapper trackedShipDtoMapper;

    @GetMapping
    public Flux<ResponseEntity<ServerSentEvent<ResponseModel<ShipTrack>>>> getShipTrackHistory() {
        return shipTrackHistoryService.getShipTrackHistory()
                .map(response ->
                        ResponseEntity.ok(
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

    @GetMapping("/tracked-ships")
    public Mono<ResponseEntity<ResponseModel<List<TrackedShipReadDto>>>> getTrackedShips() {
        return shipTrackHistoryService.getTrackedShips()
                .map(this::mapToResponseEntity);
    }

    private ResponseEntity<ResponseModel<List<TrackedShipReadDto>>> mapToResponseEntity(List<TrackedShip> trackedShips) {
        List<TrackedShipReadDto> trackedShipReadDtos = trackedShips.stream()
                .map(trackedShipDtoMapper::mapToReadDto)
                .toList();

        return ResponseEntity.ok(
                ResponseModel.<List<TrackedShipReadDto>>builder()
                        .httpStatus(OK)
                        .httpStatusCode(OK.value())
                        .body(of("TrackedShips", trackedShipReadDtos))
                        .build()
        );
    }

    @PostMapping("/tracked-ships")
    public Mono<ResponseEntity<ResponseModel<TrackedShipReadDto>>> saveTrackedShip(@RequestBody @Valid TrackedShipSaveDto trackedShipSaveDto) {
        return shipTrackHistoryService.saveTrackedShip(
                        trackedShipDtoMapper.mapFrom(
                                trackedShipSaveDto,
                                ActivePointsListHolder.getName(trackedShipSaveDto.getMmsi()))
                )
                .map(response ->
                        ResponseEntity
                                .status(CREATED)
                                .body(
                                        ResponseModel.<TrackedShipReadDto>builder()
                                                .httpStatus(CREATED)
                                                .httpStatusCode(CREATED.value())
                                                .body(of("TrackedShip", trackedShipDtoMapper.mapToReadDto(response)))
                                                .build()
                                )
                );
    }

    @DeleteMapping("/tracked-ships/{mmsi}")
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