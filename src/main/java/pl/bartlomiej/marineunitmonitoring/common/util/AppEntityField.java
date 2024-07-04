package pl.bartlomiej.marineunitmonitoring.common.util;

public enum AppEntityField {
    MMSI("mmsi"),
    READING_TIME("readingTime");

    public final String fieldName;

    AppEntityField(String fieldName) {
        this.fieldName = fieldName;
    }
}
