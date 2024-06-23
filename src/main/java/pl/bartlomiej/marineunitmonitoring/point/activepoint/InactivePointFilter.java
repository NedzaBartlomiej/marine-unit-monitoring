package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.reactive.ActivePointReactiveService;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import reactor.core.publisher.Mono;

import java.util.List;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

@Component
@Slf4j
public class InactivePointFilter {

    private final ActivePointReactiveService activePointReactiveService;
    private final ShipTrackHistoryService shipTrackHistoryService;
    private final TrackedShipService trackedShipService;

    public InactivePointFilter(
            @Qualifier("activePointReactiveServiceImpl") ActivePointReactiveService activePointReactiveService,
            ShipTrackHistoryService shipTrackHistoryService,
            TrackedShipService trackedShipService) {
        this.activePointReactiveService = activePointReactiveService;
        this.shipTrackHistoryService = shipTrackHistoryService;
        this.trackedShipService = trackedShipService;
    }

    public Mono<Void> filter(List<Long> activeMmsis) {
        return activePointReactiveService.getMmsis()
                .flatMap(actualMmsis -> {

                    if (activeMmsis.isEmpty()) {
                        return error(new MmsiConflictException("Active mmsis is empty."));
                    }

                    // exclude matching mmsis and detailing inactive mmsis
                    List<Long> inactiveMmsis = actualMmsis.stream()
                            .filter(actualMmsi -> !activeMmsis.contains(actualMmsi))
                            .toList();

                    if (inactiveMmsis.isEmpty()) {
                        log.info("All points are active.");
                    } else {
                        inactiveMmsis
                                .forEach(mmsi -> {
                                    log.info("Removing inactive point - {}", mmsi);
                                    activePointReactiveService.removeActivePoint(mmsi)
                                            .doOnError(e -> log.warn("Active points - {}", e.getMessage()))
                                            .subscribe();
                                    try {
                                        trackedShipService.removeTrackedShip(mmsi);
                                    } catch (NotFoundException e) {
                                        log.warn("Tracked ships - {}", e.getMessage());
                                    }
                                    shipTrackHistoryService.clearShipHistory(mmsi)
                                            .doOnError(e -> log.warn("Ship track history - {}", e.getMessage()))
                                            .subscribe();
                                });
                    }

                    return empty();
                })
                .doOnError(err -> log.warn("Something issue when filtering - {}", err.getMessage()))
                .then();
    }
}
