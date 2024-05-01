package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ActivePointService {

    String ASYNC_SUPPORTED = "Operation is only supported in async implementation.";
    String SYNC_SUPPORTED = "Operation is only supported in sync implementation.";

    default String getName(Long mmsi) {
        throw new UnsupportedOperationException(SYNC_SUPPORTED);
    }

    default Boolean isPointActive(Long mmsi) {
        throw new UnsupportedOperationException(SYNC_SUPPORTED);
    }

    default Mono<List<Long>> getMmsis() {
        throw new UnsupportedOperationException(ASYNC_SUPPORTED);
    }

    default Mono<Void> removeActivePoint(Long mmsi) {
        throw new UnsupportedOperationException(ASYNC_SUPPORTED);
    }

    default Mono<Void> addActivePoint(ActivePoint activePoint) {
        throw new UnsupportedOperationException(ASYNC_SUPPORTED);
    }
}
