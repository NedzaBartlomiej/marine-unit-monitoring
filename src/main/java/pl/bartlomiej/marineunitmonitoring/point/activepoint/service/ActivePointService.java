package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ActivePointService {

    Mono<List<Long>> getMmsis();

    Mono<Void> removeActivePoint(Long mmsi);

    Mono<Void> addActivePoint(ActivePoint activePoint);

    Mono<Void> isPointActive(Long mmsi);

    Mono<String> getName(Long mmsi);
}
