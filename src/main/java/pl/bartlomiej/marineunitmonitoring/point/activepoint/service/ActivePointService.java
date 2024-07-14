package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ActivePointService {

    Mono<List<String>> getMmsis();

    Mono<Void> removeActivePoint(String mmsi);

    Mono<Void> addActivePoint(ActivePoint activePoint);

    Mono<Boolean> isPointActive(String mmsi);

    Mono<String> getName(String mmsi);
}
