package pl.bartlomiej.marineunitmonitoring.ais;

public record Ais(
        String type,
        Geometry geometry,
        Properties properties) {
}