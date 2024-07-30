package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;
import reactor.core.publisher.Mono;

@Repository
public class CustomJWTEntityRepositoryImpl implements CustomJWTEntityRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomJWTEntityRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Void> updateIsValid(String id, boolean isValid) {
        return reactiveMongoTemplate.updateFirst(
                new Query(Criteria.where(JWTConstants.ID).is(id)),
                new Update().set(JWTConstants.IS_VALID, isValid),
                JWTEntity.class
        ).then();
    }

    @Override
    public Mono<Void> updateIsValidByUid(String uid, boolean isValid) {
        return reactiveMongoTemplate.updateMulti(
                new Query(Criteria.where(JWTConstants.UID).is(uid)),
                new Update().set(JWTConstants.IS_VALID, isValid),
                JWTEntity.class
        ).then();
    }
}
