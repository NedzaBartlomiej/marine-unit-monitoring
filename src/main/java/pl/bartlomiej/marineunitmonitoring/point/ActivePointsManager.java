package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pl.bartlomiej.marineunitmonitoring.point.PointServiceImpl.UNKNOWN_NOT_REPORTED;

@Slf4j
@RequiredArgsConstructor
@Component
public class ActivePointsManager {
    private static final Set<ActivePoint> activePoints = new HashSet<>();
    private static final int CLEAR_INACTIVE_SHIPS_DELAY = 1000 * 60 * 45;
    private final TrackedShipService trackedShipService;
    private final ShipTrackHistoryService shipTrackHistoryService;
    private final AisService aisService;

    public static List<Long> getMmsis() {
        return activePoints.stream()
                .map(ActivePoint::mmsi)
                .toList();
    }

    public static String getName(Long mmsi) {
        return activePoints.stream()
                .filter(activePoint -> activePoint.mmsi.equals(mmsi))
                .map(ActivePoint::name)
                .findAny()
                .orElse(UNKNOWN_NOT_REPORTED);
    }

    public static Boolean isPointActive(Long mmsi) {
        return !activePoints.stream()
                .filter(activePoint -> activePoint.mmsi.equals(mmsi))
                .toList()
                .isEmpty();
    }

    public static void addActivePoint(ActivePoint activePoint) {
        if (!isPointActive(activePoint.mmsi)) {
            activePoints.add(activePoint);
        } else {
            log.info("Point is already in list.");
        }
    }

    public static void removeActivePoint(Long mmsi) {
        if (!activePoints.removeIf(activePoint ->
                activePoint.mmsi().equals(mmsi))) {
            throw new NotFoundException();
        }
    }

    @Scheduled(initialDelay = 0, fixedDelay = CLEAR_INACTIVE_SHIPS_DELAY)
    private void clearInactiveShips() {
        aisService.fetchLatestShips()
                .map(aisShip -> aisShip.properties().mmsi())
                .collectList()
                .doOnError(error -> log.error("Something go wrong on clearing inactive ships: {}", error.getMessage()))
                .subscribe(list -> {

                    // identify inactive mmsis
                    List<Long> invalidMmsis = getMmsis();
                    invalidMmsis.removeAll(list);

                    // remove inactive ships from app
                    invalidMmsis.forEach(mmsi -> {
                        removeActivePoint(mmsi);
                        trackedShipService.removeTrackedShip(mmsi);
                        shipTrackHistoryService.clearShipHistory(mmsi).subscribe();
                        log.info("Inactive ships has been removed - {}", mmsi);
                    });
                });
    }

    public record ActivePoint(Long mmsi, String name) {
    }

}
