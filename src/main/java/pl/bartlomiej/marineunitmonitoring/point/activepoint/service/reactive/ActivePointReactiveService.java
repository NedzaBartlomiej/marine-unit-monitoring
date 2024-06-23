package pl.bartlomiej.marineunitmonitoring.point.activepoint.service.reactive;

import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ActivePointReactiveService {

    Mono<List<Long>> getMmsis();

    Mono<Void> removeActivePoint(Long mmsi);

    Mono<Void> addActivePoint(ActivePoint activePoint);
}
