package pl.bartlomiej.marineunitmonitoring.point;

//todo create unique collection of actually having points -> for validation - there where new Point
public record Point(

        Long mmsi,
        String name,
        Double pointX,
        Double pointY,
        String destinationName,
        Double destinationX,
        Double destinationY) {
}
