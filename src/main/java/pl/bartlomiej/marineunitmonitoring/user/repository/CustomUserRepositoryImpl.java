package pl.bartlomiej.marineunitmonitoring.user.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    public final String TRACKED_SHIPS_EMBEDDED_LIST_NAME = "trackedShips";
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomUserRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    private Query getIdValidQuery(String id) {
        String ID_FIELD_NAME = "_id";
        return new Query(where(ID_FIELD_NAME).is(id));
    }

    @Override
    public Mono<TrackedShip> pushTrackedShip(String id, TrackedShip trackedShip) {
        return reactiveMongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().push(TRACKED_SHIPS_EMBEDDED_LIST_NAME, trackedShip),
                        User.class
                ).map(updateResult -> trackedShip);
    }

    @Override
    public Mono<Void> pullTrackedShip(String id, String mmsi) {
        return reactiveMongoTemplate
                .updateFirst(
                        this.getIdValidQuery(id),
                        new Update().pull(TRACKED_SHIPS_EMBEDDED_LIST_NAME, query(where(MMSI.fieldName).is(mmsi))),
                        User.class
                ).then();
    }

    @Override
    public Mono<Void> pullTrackedShip(String mmsi) {
        return reactiveMongoTemplate
                .updateMulti(
                        new Query(),
                        new Update().pull(TRACKED_SHIPS_EMBEDDED_LIST_NAME, query(where(MMSI.fieldName).is(mmsi))),
                        User.class
                ).then();
    }

    @Override
    public Flux<TrackedShip> getTrackedShips(String id) {
        return reactiveMongoTemplate.findById(id, User.class)
                .flatMapIterable(User::getTrackedShips)
                .onErrorResume(NullPointerException.class, ex -> Flux.empty());
    }

    @Override
    public Flux<TrackedShip> getTrackedShips() {
        return reactiveMongoTemplate.findAll(User.class)
                .flatMapIterable(User::getTrackedShips)
                .onErrorResume(NullPointerException.class, ex -> Flux.empty());
    }

    @Override
    public Mono<User> findByOpenId(String openId) {
        return reactiveMongoTemplate.findOne(
                new Query().addCriteria(
                        Criteria.where("openIds").is(openId)
                ),
                User.class
        );
    }

    @Override
    public Mono<Void> updateIsVerified(String id, boolean isVerified) {
        return reactiveMongoTemplate.updateFirst(
                this.getIdValidQuery(id),
                new Update().set("isVerified", isVerified),
                User.class
        ).then();
    }

    @Override
    public Mono<Void> updateIsLocked(String id, boolean isLocked) {
        return reactiveMongoTemplate.updateFirst(
                this.getIdValidQuery(id),
                new Update().set("isLocked", isLocked),
                User.class
        ).then();
    }

    @Override
    public Mono<Void> updatePassword(String id, String password) {
        return reactiveMongoTemplate.updateFirst(
                this.getIdValidQuery(id),
                new Update().set("password", password),
                User.class
        ).then();
    }
}