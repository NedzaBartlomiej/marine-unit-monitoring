package pl.bartlomiej.marineunitmonitoring.ais;

import java.util.ArrayList;

public record Geometry(
        String type,
        ArrayList<Double> coordinates) {
    public static final int X_CORDS_INDEX = 0;
    public static final int Y_CORDS_INDEX = 1;
}