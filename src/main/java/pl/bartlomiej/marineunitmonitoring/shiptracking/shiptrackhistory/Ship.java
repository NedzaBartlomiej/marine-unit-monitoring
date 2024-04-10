package pl.bartlomiej.marineunitmonitoring.shiptracking.shiptrackhistory;

import java.util.Date;

public record Ship(
        Integer courseOverGround,
        Double latitude,
        Double longitude,
        String name,
        Integer rateOfTurn,
        Integer shipType,
        Double speedOverGround,
        Integer trueHeading,
        Integer navigationalStatus,
        Long mmsi,
        Date msgtime
) {
}
