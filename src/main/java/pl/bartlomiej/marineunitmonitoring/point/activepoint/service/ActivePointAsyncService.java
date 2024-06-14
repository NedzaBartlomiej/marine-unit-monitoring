package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ActivePointAsyncService {

    Mono<List<Long>> getMmsis();

    Mono<Void> removeActivePoint(Long mmsi);

    Mono<Void> addActivePoint(ActivePoint activePoint);
}
