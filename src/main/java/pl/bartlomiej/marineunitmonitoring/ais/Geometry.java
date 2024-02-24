package pl.bartlomiej.marineunitmonitoring.ais;

import java.util.ArrayList;

public record Geometry(
        String type,
        ArrayList<Double> coordinates) {
}