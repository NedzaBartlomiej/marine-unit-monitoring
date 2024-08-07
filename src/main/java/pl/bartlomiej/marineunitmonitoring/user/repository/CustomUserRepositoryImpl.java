package pl.bartlomiej.marineunitmonitoring.user.repository;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.helper.repository.CustomRepository;
import pl.bartlomiej.marineunitmonitoring.common.util.CommonShipFields;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.UserConstants;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final CustomRepository customRepository;

    public CustomUserRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate, CustomRepository customRepository) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.customRepository = customRepository;
    }

    @Override
    public Mono<TrackedShip> pushTrackedShip(String id, TrackedShip trackedShip) {
        return this.push(id, UserConstants.TRACKED_SHIPS, trackedShip)
                .then(Mono.just(trackedShip));
    }

    @Override
    public Mono<Void> pullTrackedShip(String id, String mmsi) {
        return reactiveMongoTemplate
                .updateFirst(
                        customRepository.getIdValidQuery(id),
                        new Update().pull(UserConstants.TRACKED_SHIPS, query(where(CommonShipFields.MMSI).is(mmsi))),
                        User.class
                ).then();
    }

    @Override
    public Mono<Void> pullTrackedShip(String mmsi) {
        return reactiveMongoTemplate
                .updateMulti(
                        new Query(),
                        new Update().pull(UserConstants.TRACKED_SHIPS, query(where(CommonShipFields.MMSI).is(mmsi))),
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
                        Criteria.where(UserConstants.OPEN_IDS).is(openId)
                ),
                User.class
        );
    }

    @Override
    public Flux<String> findAllEmails() {
        return reactiveMongoTemplate.findDistinct(
                UserConstants.EMAIL,
                User.class,
                String.class
        );
    }

    @Override
    public Mono<Void> pushTrustedIpAddress(String id, String ipAddress) {
        return this.push(id, UserConstants.TRUSTED_IP_ADDRESSES, ipAddress)
                .then();
    }

    private Mono<UpdateResult> push(String id, String updatedFieldName, Object pushedValue) {
        return reactiveMongoTemplate
                .updateFirst(
                        customRepository.getIdValidQuery(id),
                        new Update().push(updatedFieldName, pushedValue),
                        User.class
                );
    }
}