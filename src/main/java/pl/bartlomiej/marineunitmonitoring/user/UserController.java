package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserSaveDto;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import pl.bartlomiej.marineunitmonitoring.user.service.sync.UserService;

import java.security.Principal;
import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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

    private static <T> ResponseEntity<ResponseModel<T>> buildResponse(
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

        return ResponseEntity
                .status(httpStatus)
                .body(builder.build());
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping
    public ResponseEntity<ResponseModel<User>> getAuthenticatedUser(Principal principal) {
        Jwt jwt = (Jwt) principal;
        return buildResponse(
                null,
                OK,
                userService.getUserByOpenId(jwt.getSubject()),
                "User"
        );
    }

    @PostMapping
    public ResponseEntity<ResponseModel<User>> createUser(@RequestBody @Valid UserSaveDto userSaveDto) {
        return buildResponse(
                null,
                CREATED,
                userService.createUser(
                        userDtoMapper.mapFrom(userSaveDto)
                ),
                "User"
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return buildResponse(
                "User has been deleted successfully.",
                OK,
                null,
                null
        );
    }

    // TRACKED SHIP

    @GetMapping("/{id}/tracked-ships")
    public ResponseEntity<ResponseModel<List<TrackedShip>>> getTrackedShips(@PathVariable String id) {
        return buildResponse(
                null,
                OK,
                userTrackedShipService.getTrackedShips(id),
                "TrackedShips"
        );
    }

    @PostMapping("/{id}/tracked-ships/{mmsi}")
    public ResponseEntity<ResponseModel<TrackedShip>> addTrackedShip(@PathVariable String id, @PathVariable Long mmsi) {
        return buildResponse(
                "Successfully added ship into tracking list.",
                CREATED,
                userTrackedShipService.addTrackedShip(id, mmsi),
                "TrackedShip"
        );
    }

    @DeleteMapping("/{id}/tracked-ships/{mmsi}")
    public ResponseEntity<ResponseModel<TrackedShip>> removeTrackedShip(@PathVariable String id, @PathVariable Long mmsi) {
        userTrackedShipService.removeTrackedShip(id, mmsi);
        return buildResponse(
                "Successfully removed ship from tracking list.",
                OK,
                null,
                null
        );
    }
}
