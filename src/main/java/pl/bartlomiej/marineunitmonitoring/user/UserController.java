package pl.bartlomiej.marineunitmonitoring.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.service.EmailVerificationService;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserDtoMapper;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserReadDto;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserSaveDto;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final TrackedShipService userTrackedShipService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final EmailVerificationService emailVerificationService;
    private final TransactionalOperator transactionalOperator;

    public UserController(TrackedShipService userTrackedShipService, UserService userService, UserDtoMapper userDtoMapper, EmailVerificationService emailVerificationService, TransactionalOperator transactionalOperator) {
        this.userTrackedShipService = userTrackedShipService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.emailVerificationService = emailVerificationService;
        this.transactionalOperator = transactionalOperator;
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping
    public Mono<ResponseEntity<ResponseModel<UserReadDto>>> getAuthenticatedUser(Principal principal) {
        return userService.identifyUser(principal.getName())
                .flatMap(id -> userService.getUser(id)
                        .map(user ->
                                ControllerResponseUtil.buildResponse(
                                        OK,
                                        ControllerResponseUtil.buildResponseModel(
                                                null,
                                                OK,
                                                userDtoMapper.mapToReadDto(user),
                                                "user"
                                        )
                                )
                        )
                );
    }

    @PostMapping
    public Mono<ResponseEntity<ResponseModel<UserReadDto>>> createUser(@RequestBody @Valid UserSaveDto userSaveDto) {
        return this.transactionalUserCreationProcess(userSaveDto)
                .map(user ->
                        buildResponse(
                                CREATED,
                                buildResponseModel(
                                        "Your account is currently unavailable, you need to verify your email address, the message has been sent to your mailbox.",
                                        CREATED,
                                        userDtoMapper.mapToReadDto(user),
                                        "user"
                                )
                        )
                );
    }

    private Mono<User> transactionalUserCreationProcess(UserSaveDto userSaveDto) {
        return transactionalOperator.transactional(
                userService.createUser(userDtoMapper.mapFrom(userSaveDto))
                        .flatMap(user ->
                                emailVerificationService.issueVerificationToken(user.getId())
                                        .then(just(user))
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

    @PreAuthorize("hasAnyRole(" +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name()," +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name()" +
            ")"
    )
    @GetMapping("/tracked-ships") // todo pageable
    public ResponseEntity<Flux<ResponseModel<TrackedShip>>> getTrackedShips(Principal principal) {
        return ok(userService.identifyUser(principal.getName())
                .flatMapMany(id -> userTrackedShipService.getTrackedShips(id)
                        .map(trackedShip ->
                                buildResponseModel(
                                        null,
                                        OK,
                                        trackedShip,
                                        "trackedShip"
                                )
                        )
                )
        );
    }

    @PreAuthorize("hasAnyRole(" +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name()," +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name()" +
            ")"
    )
    @PostMapping("/tracked-ships/{mmsi}")
    public Mono<ResponseEntity<ResponseModel<TrackedShip>>> addTrackedShip(Principal principal, @PathVariable String mmsi) {
        return userService.identifyUser(principal.getName())
                .flatMap(id -> userTrackedShipService.addTrackedShip(id, mmsi)
                        .map(trackedShip ->
                                buildResponse(
                                        CREATED,
                                        buildResponseModel(
                                                "Successfully added ship into tracking list.",
                                                CREATED,
                                                trackedShip,
                                                "trackedShip"
                                        )
                                )
                        )
                );
    }

    @PreAuthorize("hasAnyRole(" +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).PREMIUM.name()," +
            "T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name()" +
            ")"
    )
    @DeleteMapping("/tracked-ships/{mmsi}")
    public Mono<ResponseEntity<ResponseModel<Void>>> removeTrackedShip(Principal principal, @PathVariable String mmsi) {

        return userService.identifyUser(principal.getName())
                .flatMap(id -> userTrackedShipService.removeTrackedShip(id, mmsi)
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
                        ))
                );
    }
}