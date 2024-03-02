package pl.bartlomiej.marineunitmonitoring.ais;

import pl.bartlomiej.marineunitmonitoring.point.Point;
import reactor.core.publisher.Flux;

public interface AisService {
    Flux<Point> getLatestAisPoints();
}
