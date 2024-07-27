package pl.bartlomiej.marineunitmonitoring.shiptracking;

import pl.bartlomiej.marineunitmonitoring.common.util.CommonFields;
import pl.bartlomiej.marineunitmonitoring.common.util.CommonShipFields;

public final class ShipTrackConstants implements CommonFields, CommonShipFields {

    public static final String SHIP_TRACKS_COLLECTION = "ship_tracks";
    public static final String READING_TIME = "readingTime";

    private ShipTrackConstants() {
    }
}
