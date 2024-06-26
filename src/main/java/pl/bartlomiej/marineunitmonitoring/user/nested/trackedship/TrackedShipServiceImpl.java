package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.SHIP_IS_ALREADY_TRACKED;
import static reactor.core.publisher.Flux.error;

@Service
@Slf4j
public class TrackedShipServiceImpl implements TrackedShipService {

    private final UserService userService;
    private final CustomUserRepository customUserRepository;
    private final ActivePointService activePointService;

    public TrackedShipServiceImpl(
            MongoUserRepository mongoUserRepository, UserService userService,
            CustomUserRepository customUserRepository,
            ActivePointService activePointService) {
        this.userService = userService;
        this.customUserRepository = customUserRepository;
        this.activePointService = activePointService;
    }


    public Flux<TrackedShip> getTrackedShips(String id) {
        return customUserRepository.getTrackedShips(id)
                .switchIfEmpty(error(NoContentException::new));
    }

    @Transactional
    @Override
    public Mono<TrackedShip> addTrackedShip(String id, Long mmsi) {
        return userService.isUserExists(id)
                .then(activePointService.isPointActive(mmsi))
                .then(this.isShipTrackedMono(id, mmsi))
                .then(activePointService.getName(mmsi)
                        .map(name -> new TrackedShip(mmsi, name))
                )
                .flatMap(trackedShip -> customUserRepository.pushTrackedShip(id, trackedShip));
    }


    @Transactional
    @Override
    public Mono<Void> removeTrackedShip(String id, Long mmsi) {
        return userService.isUserExists(id)
                .then(this.isShipTrackedMono(mmsi))
                .then(customUserRepository.pullTrackedShip(id, mmsi));
    }

    @Transactional
    @Override
    public Mono<Void> removeTrackedShip(Long mmsi) {
        return this.isShipTrackedMono(mmsi)
                .then(customUserRepository.pullTrackedShip(mmsi));
    }


    private Mono<Void> isShipTrackedMono(String id, Long mmsi) {
        return this.isShipTracked(id, mmsi)
                .flatMap(isTracked -> {
                    if (isTracked) {
                        return Mono.error(new MmsiConflictException(SHIP_IS_ALREADY_TRACKED.message));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> isShipTrackedMono(Long mmsi) {
        return this.isShipTracked(mmsi)
                .flatMap(isTracked -> {
                    if (isTracked) {
                        return Mono.error(new MmsiConflictException(SHIP_IS_ALREADY_TRACKED.message));
                    }
                    return Mono.empty();
                });
    }


    private Mono<Boolean> isShipTracked(String id, Long mmsi) {
        return customUserRepository.getTrackedShips(id)
                .any(trackedShip -> trackedShip.getMmsi().equals(mmsi));
    }

    private Mono<Boolean> isShipTracked(Long mmsi) {
        return customUserRepository.getTrackedShips()
                .any(trackedShip -> trackedShip.getMmsi().equals(mmsi));
    }
}
