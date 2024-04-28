package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ActivePointRepository extends MongoRepository<ActivePoint, String> {

    Boolean existsByMmsi(Long mmsi);

    Optional<ActivePoint> findByMmsi(Long mmsi);

    void deleteByMmsi(Long mmsi);
}
