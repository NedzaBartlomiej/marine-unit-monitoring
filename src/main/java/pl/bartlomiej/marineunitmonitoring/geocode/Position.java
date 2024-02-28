package pl.bartlomiej.marineunitmonitoring.geocode;

import java.io.Serializable;

public record Position(
        Double lat,
        Double lng
) implements Serializable {
}