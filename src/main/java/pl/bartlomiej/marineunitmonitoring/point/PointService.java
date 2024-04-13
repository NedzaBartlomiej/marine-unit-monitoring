package pl.bartlomiej.marineunitmonitoring.point;

import reactor.core.publisher.Flux;

public interface PointService {

    Flux<Point> getPoints();
}
