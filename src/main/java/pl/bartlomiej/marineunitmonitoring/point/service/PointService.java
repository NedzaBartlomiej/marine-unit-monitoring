package pl.bartlomiej.marineunitmonitoring.point.service;

import pl.bartlomiej.marineunitmonitoring.point.Point;
import reactor.core.publisher.Flux;

public interface PointService {

    Flux<Point> getPoints();
}
