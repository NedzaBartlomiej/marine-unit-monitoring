package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenConstants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public class CustomVerificationTokenRepositoryImpl implements CustomVerificationTokenRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomVerificationTokenRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<VerificationToken> findExpiredTokens() {
        return reactiveMongoTemplate.find(
                new Query(
                        Criteria.where(VerificationTokenConstants.EXPIRATION).lte(LocalDateTime.now())
                ),
                VerificationToken.class
        );
    }

    @Override
    public Mono<Void> updateIsVerified(String id, boolean isVerified) {
        return reactiveMongoTemplate.updateFirst(
                new Query(Criteria.where(VerificationTokenConstants.ID).is(id)),
                new Update().set(VerificationTokenConstants.IS_VERIFIED, isVerified),
                VerificationToken.class
        ).then();
    }
}
