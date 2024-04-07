package pl.bartlomiej.marineunitmonitoring.user.repository;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    public static final String TRACKED_SHIPS = "trackedShips";
    private final MongoTemplate mongoTemplate;

    private Query getIdValidQuery(String id) {
        return new Query(where("_id").is(new ObjectId(id)));
    }

    @Override
    public TrackedShip pushTrackedShip(String id, TrackedShip trackedShip) {
        mongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().push(TRACKED_SHIPS, trackedShip),
                        User.class
                );
        return trackedShip;
    }

    @Override
    public void pullTrackedShip(String id, Long mmsi) {
        mongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().pull(TRACKED_SHIPS, query(where("mmsi").is(mmsi))),
                        User.class
                );
    }

    @Override
    public List<TrackedShip> getTrackedShips(String id) {
        return requireNonNull(mongoTemplate.findById(
                new ObjectId(id),
                User.class
        )).getTrackedShips();
    }

}