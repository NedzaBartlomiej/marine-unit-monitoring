package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserReadDto;
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
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final TrackedShipService userTrackedShipService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    public UserController(TrackedShipService userTrackedShipService, @Qualifier("userServiceImpl") UserService userService, UserDtoMapper userDtoMapper) {
        this.userTrackedShipService = userTrackedShipService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
    }

    private <T> ResponseEntity<ResponseModel<T>> buildResponse(HttpStatus httpStatus, ResponseModel<T> responseModel) {
        return ResponseEntity.status(httpStatus).body(responseModel);
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
    public Mono<ResponseEntity<ResponseModel<UserReadDto>>> getAuthenticatedUser(Principal principal) {
        log.info("Principal getName(): {}", principal.getName());
        return userService.getUserById(principal.getName())
                .map(user ->
                        this.buildResponse(
                                OK,
                                buildResponseModel(
                                        null,
                                        OK,
                                        userDtoMapper.mapToReadDto(user),
                                        "User"
                                )
                        )
                );
    }

    @PostMapping
    public Mono<ResponseEntity<ResponseModel<UserReadDto>>> createUser(@RequestBody @Valid UserSaveDto userSaveDto) {
        return userService.createUser(userDtoMapper.mapFrom(userSaveDto))
                .map(user ->
                        buildResponse(
                                CREATED,
                                buildResponseModel(
                                        null,
                                        CREATED,
                                        userDtoMapper.mapToReadDto(user),
                                        "User"
                                )
                        )
                );
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name())")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ResponseModel<Void>>> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id)
                .then(just(
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        "User has been deleted successfully.",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }

    // TRACKED SHIP

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name())")
    @GetMapping("/tracked-ships") // todo pageable
    public ResponseEntity<Flux<ResponseModel<TrackedShip>>> getTrackedShips(Principal principal) {
        return ok(
                userTrackedShipService.getTrackedShips(principal.getName())
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

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name())")
    @PostMapping("/tracked-ships/{mmsi}")
    public Mono<ResponseEntity<ResponseModel<TrackedShip>>> addTrackedShip(Principal principal, @PathVariable Long mmsi) {
        return userTrackedShipService.addTrackedShip(principal.getName(), mmsi)
                .map(trackedShip ->
                        buildResponse(
                                CREATED,
                                buildResponseModel(
                                        "Successfully added ship into tracking list.",
                                        CREATED,
                                        trackedShip,
                                        "TrackedShip"
                                )
                        )
                );
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name())")
    @DeleteMapping("/tracked-ships/{mmsi}")
    public Mono<ResponseEntity<ResponseModel<Void>>> removeTrackedShip(Principal principal, @PathVariable Long mmsi) {

        return userTrackedShipService.removeTrackedShip(principal.getName(), mmsi)
                .then(just(
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        "Successfully removed ship from tracking list.",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }
}