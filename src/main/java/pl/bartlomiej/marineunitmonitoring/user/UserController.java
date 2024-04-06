package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserSaveDto;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;


    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<User>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(
                ResponseModel.<User>builder()
                        .httpStatus(OK)
                        .httpStatusCode(OK.value())
                        .body(
                                of(
                                        "User",
                                        userService.getUser(id)
                                )
                        )
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ResponseModel<User>> createUser(@RequestBody @Valid UserSaveDto userSaveDto) {
        return ResponseEntity.ok(
                ResponseModel.<User>builder()
                        .httpStatus(OK)
                        .httpStatusCode(OK.value())
                        .body(
                                of(
                                        "User",
                                        userService.createUser(
                                                userDtoMapper.mapFrom(userSaveDto)
                                        )
                                )
                        )
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ResponseModel.<Void>builder()
                        .httpStatus(OK)
                        .httpStatusCode(OK.value())
                        .message("User has been deleted successfully.")
                        .build()
        );
    }


    @PatchMapping("/{id}/tracked-ships/{mmsi}")
    public ResponseEntity<ResponseModel<TrackedShip>> addTrackedShip(@PathVariable String id, @PathVariable Long mmsi) {
        return ResponseEntity.ok(
                ResponseModel.<TrackedShip>builder()
                        .httpStatus(OK)
                        .httpStatusCode(OK.value())
                        .body(
                                of(
                                        "TrackedShip", userService.addTrackedShip(id, mmsi)
                                )
                        )
                        .message("Successfully added ship into tracking list.")
                        .build()
        );
    }

}
