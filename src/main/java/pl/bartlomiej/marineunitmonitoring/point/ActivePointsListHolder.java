package pl.bartlomiej.marineunitmonitoring.point;

import java.util.HashSet;
import java.util.Set;

public class ActivePointsListHolder {
    private static final Set<Long> activePointsMmsis = new HashSet<>();

    public static Boolean isMmsiActive(Long mmsi) {
        return activePointsMmsis.contains(mmsi);
    }

    public static void addActivePointMmsi(Long pointMmsi) {
        activePointsMmsis.add(pointMmsi);
    }
}
