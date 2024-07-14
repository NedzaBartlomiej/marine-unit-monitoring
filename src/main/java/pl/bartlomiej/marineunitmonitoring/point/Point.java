package pl.bartlomiej.marineunitmonitoring.point;

import java.io.Serializable;

public record Point(

        String mmsi,
        String name,
        Double pointX,
        Double pointY,
        String destinationName,
        Double destinationX,
        Double destinationY) implements Serializable {
}
