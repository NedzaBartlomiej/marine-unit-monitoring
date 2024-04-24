package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pl.bartlomiej.marineunitmonitoring.point.PointServiceImpl.UNKNOWN_NOT_REPORTED;

@Slf4j
@RequiredArgsConstructor
@Component
public class ActivePointsManager {
    private static final Set<ActivePoint> activePoints = new HashSet<>(); // todo store it in db
    private final TrackedShipService trackedShipService;
    private final ShipTrackHistoryService shipTrackHistoryService;

    public static List<Long> getMmsis(boolean shouldThrowIfEmpty) {
        List<Long> mmsis = activePoints.stream()
                .map(ActivePoint::mmsi)
                .toList();
        if (mmsis.isEmpty() && shouldThrowIfEmpty) {
            throw new MmsiConflictException("No active points.");
        }
        return mmsis;
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

    public static void removeActivePoint(Long mmsi) {
        if (!activePoints.removeIf(activePoint ->
                activePoint.mmsi().equals(mmsi))) {
            log.info("The point to be removed is not on the list.");
        }
    }

    public void addActivePoint(ActivePoint activePoint) {
        if (!isPointActive(activePoint.mmsi)) {
            activePoints.add(activePoint);
        } else {
            log.info("Point is already in list.");
        }
    }

    public void filterInactiveShips(List<Long> activeMmsis) {
        if (!getMmsis(false).isEmpty() && !activeMmsis.isEmpty()) {
            List<Long> actualMmsis = new ArrayList<>(getMmsis(false));
            List<Long> inactiveMmsis = activeMmsis.stream()
                    .filter(activeMmsi -> !actualMmsis.contains(activeMmsi))
                    .toList();

            if (inactiveMmsis.isEmpty()) {
                log.info("All points are active.");
            } else {
                inactiveMmsis
                        .forEach(mmsi -> {
                            log.info("Removing inactive point - {}", mmsi);
                            removeActivePoint(mmsi);
                            trackedShipService.removeTrackedShip(mmsi);
                            shipTrackHistoryService.clearShipHistory(mmsi).subscribe();
                        });
            }
        } else {
            log.info("No points to filter.");
        }
    }

    public record ActivePoint(Long mmsi, String name) {
    }

}
