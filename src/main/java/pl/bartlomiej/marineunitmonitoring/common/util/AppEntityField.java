package pl.bartlomiej.marineunitmonitoring.common.util;

public enum AppEntityField {
    SHIP_TRACK_HISTORY("ship_track_history"),
    ID("_id"),
    MMSI("mmsi"),
    READING_TIME("readingTime"),
    X("x"),
    Y("y");

    public final String fieldName;

    AppEntityField(String fieldName) {
        this.fieldName = fieldName;
    }
}
