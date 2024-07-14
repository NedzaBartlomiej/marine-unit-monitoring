package pl.bartlomiej.marineunitmonitoring.point.activepoint.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;

public interface MongoActivePointRepository extends ReactiveMongoRepository<ActivePoint, String> {
}