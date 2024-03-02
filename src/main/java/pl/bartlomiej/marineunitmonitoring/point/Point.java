package pl.bartlomiej.marineunitmonitoring.point;

public record Point(

        Long mmsi,
        String name,
        Double pointX,
        Double pointY,
        String destinationName,
        Double destinationX,
        Double destinationY) {
}
