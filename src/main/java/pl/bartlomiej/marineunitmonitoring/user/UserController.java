package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserSaveDto;

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

}
