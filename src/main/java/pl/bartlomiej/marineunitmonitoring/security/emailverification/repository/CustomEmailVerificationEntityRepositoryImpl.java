package pl.bartlomiej.marineunitmonitoring.security.emailverification.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.security.emailverification.EmailVerificationEntity;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public class CustomEmailVerificationEntityRepositoryImpl implements CustomEmailVerificationEntityRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomEmailVerificationEntityRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<EmailVerificationEntity> findExpiredTokens() {
        return reactiveMongoTemplate.find(
                new Query(
                        Criteria.where("expiration").lte(LocalDateTime.now())
                ),
                EmailVerificationEntity.class
        );
    }
}
