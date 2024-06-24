package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.sync.ActivePointsSyncService;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Flux.error;

@Service
@Slf4j
public class TrackedShipServiceImpl implements TrackedShipService {

    private final MongoUserRepository mongoUserRepository;
    private final CustomUserRepository customUserRepository;
    private final ActivePointsSyncService activePointsSyncService;

    public TrackedShipServiceImpl(
            MongoUserRepository mongoUserRepository,
            CustomUserRepository customUserRepository,
            ActivePointsSyncService activePointsSyncService) {
        this.mongoUserRepository = mongoUserRepository;
        this.customUserRepository = customUserRepository;
        this.activePointsSyncService = activePointsSyncService;
    }


    public Flux<TrackedShip> getTrackedShips(String id) {
        return customUserRepository.getTrackedShips(id)
                .switchIfEmpty(error(NoContentException::new));
    }

    @Transactional
    @Override
    public Mono<TrackedShip> addTrackedShip(String id, Long mmsi) {
        TrackedShip toSave = new TrackedShip(mmsi, activePointsSyncService.getName(mmsi));
        return this.userExistsMono(id)
                .then(this.isPointActiveMono(mmsi))
                .then(this.isShipTrackedMono(id, mmsi))
                .then(customUserRepository.pushTrackedShip(id, toSave));
    }


    @Transactional
    @Override
    public Mono<Void> removeTrackedShip(String id, Long mmsi) {
        return this.userExistsMono(id)
                .then(this.isShipTrackedMono(mmsi))
                .then(customUserRepository.pullTrackedShip(id, mmsi));
    }

    @Transactional
    @Override
    public Mono<Void> removeTrackedShip(Long mmsi) {
        return this.isShipTrackedMono(mmsi)
                .then(customUserRepository.pullTrackedShip(mmsi));
    }


    private Mono<Void> userExistsMono(String id) {
        return mongoUserRepository.findById(id)
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .then();
    }

    private Mono<Void> isPointActiveMono(Long mmsi) {
        return activePointsSyncService.isPointActive(mmsi)
                .flatMap(isActive -> {
                    if (!isActive) {
                        return Mono.error(new IllegalArgumentException("Point is not active"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> isShipTrackedMono(String id, Long mmsi) {
        return this.isShipTracked(id, mmsi)
                .flatMap(isTracked -> {
                    if (isTracked) {
                        return Mono.error(new IllegalArgumentException("Ship is already tracked"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> isShipTrackedMono(Long mmsi) {
        return this.isShipTracked(mmsi)
                .flatMap(isTracked -> {
                    if (isTracked) {
                        return Mono.error(new IllegalArgumentException("Ship is already tracked"));
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
