package pl.bartlomiej.marineunitmonitoring.point.activepoint.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.repository.ActivePointReactiveRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import reactor.core.publisher.Mono;

import java.util.List;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActivePointAsyncManager implements ActivePointManager {

    private final ActivePointReactiveRepository activePointReactiveRepository;
    private final TrackedShipService trackedShipService;
    private final ShipTrackHistoryService shipTrackHistoryService;

    // IMPLEMENTED/SUPPORTED ASYNC METHODS

    @Override
    public Mono<List<Long>> getMmsis() {
        return activePointReactiveRepository.findAll()
                .map(ActivePoint::getMmsi)
                .collectList()
                .switchIfEmpty(error(new MmsiConflictException("No active points found.")));
    }

    @Override
    public Mono<Void> removeActivePoint(Long mmsi) {
        return activePointReactiveRepository.existsByMmsi(mmsi)
                .flatMap(exists -> {
                    if (!exists) {
                        return error(new NotFoundException());
                    }
                    return activePointReactiveRepository.deleteByMmsi(mmsi);
                });
    }

    @Override
    public Mono<Void> addActivePoint(ActivePoint activePoint) {
        return activePointReactiveRepository.existsByMmsi(activePoint.getMmsi())
                .flatMap(exists -> {
                    if (exists) {
                        return error(new MmsiConflictException("Point already exists."));
                    }
                    return activePointReactiveRepository.save(activePoint).then();
                });
    }

    @Override
    public Mono<Void> filterInactiveShips(List<Long> activeMmsis) {
        return this.getMmsis()
                .flatMap(actualMmsis -> {

                    if (activeMmsis.isEmpty()) {
                        return error(new MmsiConflictException("Active mmsis is empty."));
                    }

                    // exclude matching mmsis and detailing inactive mmsis
                    List<Long> inactiveMmsis = activeMmsis.stream()
                            .filter(activeMmsi -> !actualMmsis.contains(activeMmsi))
                            .toList();

                    if (inactiveMmsis.isEmpty()) {
                        log.info("All points are active.");
                    } else {
                        inactiveMmsis
                                .forEach(mmsi -> {
                                    log.info("Removing inactive point - {}", mmsi);
                                    this.removeActivePoint(mmsi).subscribe();
                                    trackedShipService.removeTrackedShip(mmsi);
                                    shipTrackHistoryService.clearShipHistory(mmsi).subscribe();
                                });
                    }

                    return empty();
                })
                .doOnError(err -> log.warn("Something issue when filtering - {}", err.getMessage()))
                .then();
    }
}
