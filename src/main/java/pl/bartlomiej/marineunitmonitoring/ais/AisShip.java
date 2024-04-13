package pl.bartlomiej.marineunitmonitoring.ais;

import pl.bartlomiej.marineunitmonitoring.ais.nested.Geometry;
import pl.bartlomiej.marineunitmonitoring.ais.nested.Properties;

public record AisShip(
        String type,
        Geometry geometry,
        Properties properties) {
}