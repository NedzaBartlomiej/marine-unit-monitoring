package pl.bartlomiej.marineunitmonitoring.map;

public record Point(
        String name,
        double pointX,
        double pointY,
        String destinationName,
        double destinationX,
        double destinationY) {
}
