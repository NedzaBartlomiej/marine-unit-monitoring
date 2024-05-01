package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import reactor.core.publisher.Mono;

import java.util.List;

import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

@Component
@Slf4j
public class InactivePointFilter {

    private final ActivePointService activePointService;
    private final ShipTrackHistoryService shipTrackHistoryService;
    private final TrackedShipService trackedShipService;

    public InactivePointFilter(
            @Qualifier("activePointAsyncService") ActivePointService activePointService,
            ShipTrackHistoryService shipTrackHistoryService,
            TrackedShipService trackedShipService) {
        this.activePointService = activePointService;
        this.shipTrackHistoryService = shipTrackHistoryService;
        this.trackedShipService = trackedShipService;
    }

    public Mono<Void> filter(List<Long> activeMmsis) {
        return activePointService.getMmsis()
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
                                    activePointService.removeActivePoint(mmsi).subscribe();
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
