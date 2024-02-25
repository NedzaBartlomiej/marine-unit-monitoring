package pl.bartlomiej.marineunitmonitoring.point;

public record Point(
        String name,
        double pointX,
        double pointY,
        String destinationName,
        double destinationX,
        double destinationY) {
}
