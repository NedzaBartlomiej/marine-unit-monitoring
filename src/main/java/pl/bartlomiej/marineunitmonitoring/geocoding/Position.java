package pl.bartlomiej.marineunitmonitoring.geocoding;

import java.io.Serializable;

public record Position(
        Double x,
        Double y
) implements Serializable {
}