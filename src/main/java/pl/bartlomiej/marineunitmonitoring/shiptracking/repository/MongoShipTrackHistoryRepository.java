package pl.bartlomiej.marineunitmonitoring.shiptracking.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;

public interface MongoShipTrackHistoryRepository extends ReactiveMongoRepository<ShipTrack, String> {

}
