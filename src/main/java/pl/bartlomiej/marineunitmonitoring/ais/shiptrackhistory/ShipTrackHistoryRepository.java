package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, Long> {
}
