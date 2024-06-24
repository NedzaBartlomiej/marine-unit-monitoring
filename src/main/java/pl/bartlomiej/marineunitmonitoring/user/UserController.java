package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserSaveDto;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/users")
public class UserController { // todo - make all reactive

    private final TrackedShipService userTrackedShipService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    public UserController(TrackedShipService userTrackedShipService, @Qualifier("userServiceImpl") UserService userService, UserDtoMapper userDtoMapper) {
        this.userTrackedShipService = userTrackedShipService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
    }

    private <T> ResponseModel<T> buildResponseModel(
            String message, HttpStatus httpStatus, T bodyValue, String bodyKey) {

        ResponseModel.ResponseModelBuilder<T> builder =
                ResponseModel.<T>builder()
                        .httpStatus(httpStatus)
                        .httpStatusCode(httpStatus.value());

        if (message != null) {
            builder.message(message);
        }

        if (bodyValue != null && bodyKey != null) {
            builder.body(of(bodyKey, bodyValue));
        }

        return builder.build();
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping
    public ResponseEntity<Mono<ResponseModel<User>>> getAuthenticatedUser(Principal principal) {
        return ok(
                userService.getUserByOpenId(principal.getName())
                        .map(user ->
                                buildResponseModel(
                                        null,
                                        OK,
                                        user,
                                        "User"
                                )
                        )
        );
    }

    @PostMapping
    public ResponseEntity<Mono<ResponseModel<User>>> createUser(@RequestBody @Valid UserSaveDto userSaveDto) {
        return ResponseEntity.status(CREATED).body(
                userService.createUser(userDtoMapper.mapFrom(userSaveDto))
                        .map(user ->
                                buildResponseModel(
                                        null,
                                        CREATED,
                                        user,
                                        "User"
                                )
                        )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Mono<ResponseModel<Void>>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id).subscribe();
        return ok(just(
                        buildResponseModel(
                                "User has been deleted successfully.",
                                OK,
                                null,
                                null
                        )
                )
        );
    }

    // TRACKED SHIP // todo - these endpoints also do with Principal

    @GetMapping("/{id}/tracked-ships")
    public ResponseEntity<Flux<ResponseModel<TrackedShip>>> getTrackedShips(@PathVariable String id) {
        return ok(
                userTrackedShipService.getTrackedShips(id)
                        .map(trackedShip ->
                                buildResponseModel(
                                        null,
                                        OK,
                                        trackedShip,
                                        "TrackedShip"
                                )
                        )
        );
    }

    @PostMapping("/{id}/tracked-ships/{mmsi}")
    public ResponseEntity<Mono<ResponseModel<TrackedShip>>> addTrackedShip(@PathVariable String id, @PathVariable Long mmsi) {
        return ResponseEntity.status(CREATED).body(
                userTrackedShipService.addTrackedShip(id, mmsi)
                        .map(trackedShip ->
                                buildResponseModel(
                                        "Successfully added ship into tracking list.",
                                        CREATED,
                                        trackedShip,
                                        "TrackedShip"
                                )
                        )
        );
    }

    @DeleteMapping("/{id}/tracked-ships/{mmsi}")
    public ResponseEntity<Mono<ResponseModel<Void>>> removeTrackedShip(@PathVariable String id, @PathVariable Long mmsi) {
        userTrackedShipService.removeTrackedShip(id, mmsi).subscribe();
        return ok(
                just(
                        buildResponseModel(
                                "Successfully removed ship from tracking list.",
                                OK,
                                null,
                                null
                        )
                )
        );
    }
}
