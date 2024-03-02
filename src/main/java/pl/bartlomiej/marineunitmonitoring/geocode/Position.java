package pl.bartlomiej.marineunitmonitoring.geocode;

import java.io.Serializable;

public record Position(
        Double x,
        Double y
) implements Serializable {
}