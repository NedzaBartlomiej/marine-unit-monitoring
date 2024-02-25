package pl.bartlomiej.marineunitmonitoring.geocode;

import java.io.Serializable;

public record Position(
        double lat,
        double lng
) implements Serializable {
}