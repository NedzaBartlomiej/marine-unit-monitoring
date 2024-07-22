package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import reactor.core.publisher.Flux;

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
                        Criteria.where("expiration").lte(LocalDateTime.now())
                ),
                VerificationToken.class
        );
    }
}
