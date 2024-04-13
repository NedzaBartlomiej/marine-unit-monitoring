package pl.bartlomiej.marineunitmonitoring.ais.nested;

import java.util.ArrayList;

public record Geometry(
        String type,
        ArrayList<Double> coordinates) {
    public static final int X_COORDINATE_INDEX = 0;
    public static final int Y_COORDINATE_INDEX = 1;
}