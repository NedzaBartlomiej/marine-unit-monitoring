package pl.bartlomiej.marineunitmonitoring.point;

import lombok.extern.slf4j.Slf4j;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;

import java.util.HashSet;
import java.util.Set;

import static pl.bartlomiej.marineunitmonitoring.ais.AisServiceImpl.UNKNOWN_NOT_REPORTED;

@Slf4j
public class ActivePointsListHolder {
    private static final Set<ActivePointInfo> activePoints = new HashSet<>();

    public static Boolean isPointActive(Long mmsi) {
        return !activePoints.stream()
                .filter(activePointInfo -> activePointInfo.mmsi.equals(mmsi))
                .toList()
                .isEmpty();
    }

    public static String getName(Long mmsi) {
        return activePoints.stream()
                .filter(activePointInfo -> activePointInfo.mmsi.equals(mmsi))
                .map(ActivePointInfo::name)
                .findAny()
                .orElse(UNKNOWN_NOT_REPORTED);
    }

    public static void addActivePoint(ActivePointInfo activePointInfo) {
        if (!isPointActive(activePointInfo.mmsi)) {
            activePoints.add(activePointInfo);
        } else {
            log.info("ActivePoints: Point is already in list.");
        }
    }

    public static void removeActivePoint(Long mmsi) {
        if (!activePoints.removeIf(activePointInfo ->
                activePointInfo.mmsi().equals(mmsi))) {
            throw new NotFoundException();
        }
    }

    public record ActivePointInfo(Long mmsi, String name) {
    }
}
