package pl.bartlomiej.marineunitmonitoring.point.activepoint.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import reactor.core.publisher.Mono;

public interface ActivePointReactiveRepository extends ReactiveMongoRepository<ActivePoint, String> {

    Mono<Boolean> existsByMmsi(Long mmsi);

    Mono<Void> deleteByMmsi(Long mmsi);
}