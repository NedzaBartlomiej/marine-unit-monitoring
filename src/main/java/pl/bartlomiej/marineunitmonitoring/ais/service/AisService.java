package pl.bartlomiej.marineunitmonitoring.ais.service;

import pl.bartlomiej.marineunitmonitoring.map.Point;
import reactor.core.publisher.Flux;

public interface AisService {
    Flux<Point> getLatestAisPoints();
}
