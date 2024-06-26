package pl.bartlomiej.marineunitmonitoring.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.ID;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    public static final String TRACKED_SHIPS = "trackedShips";
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private Query getIdValidQuery(String id) {
        return new Query(where(ID.fieldName).is(id));
    }

    @Override
    public Mono<TrackedShip> pushTrackedShip(String id, TrackedShip trackedShip) {
        return reactiveMongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().push(TRACKED_SHIPS, trackedShip),
                        User.class
                ).map(updateResult -> trackedShip);
    }

    @Override
    public Mono<Void> pullTrackedShip(String id, Long mmsi) {
        return reactiveMongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().pull(TRACKED_SHIPS, query(where(MMSI.fieldName).is(mmsi))),
                        User.class
                ).then();
    }

    @Override
    public Mono<Void> pullTrackedShip(Long mmsi) {
        return reactiveMongoTemplate
                .updateMulti(
                        new Query(),
                        new Update().pull(TRACKED_SHIPS, query(where(MMSI.fieldName).is(mmsi))),
                        User.class
                ).then();
    }

    @Override
    public Flux<TrackedShip> getTrackedShips(String id) {
        return reactiveMongoTemplate.findById(
                id,
                User.class
        ).flatMapIterable(User::getTrackedShips);
    }

    @Override
    public Flux<TrackedShip> getTrackedShips() {
        return reactiveMongoTemplate.findAll(User.class)
                .flatMapIterable(User::getTrackedShips);
    }

}