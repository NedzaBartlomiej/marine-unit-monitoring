package pl.bartlomiej.marineunitmonitoring.point;

import lombok.extern.slf4j.Slf4j;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ActivePointsListHolder {
    private static final Set<Map<Long, String>> activePoints = new HashSet<>();

    public static Boolean isMmsiActive(Long mmsi) {
        return !activePoints.stream()
                .filter(shipBasicInfo -> shipBasicInfo.containsKey(mmsi))
                .toList()
                .isEmpty();
    }

    public static String getName(Long mmsi) {
        try {
            return activePoints.stream()
                    .map(pointShipBasicInfo ->
                            pointShipBasicInfo.get(mmsi)
                    )
                    .findFirst()
                    .orElseThrow(NotFoundException::new);
        } catch (NotFoundException e) {
            log.error("Not found ship name by mmsi in 'activePoints'.");
            throw new NotFoundException();
        }
    }

    public static void addActivePointMmsi(Map<Long, String> pointShipBasicInfo) {
        activePoints.add(pointShipBasicInfo);
    }
}
