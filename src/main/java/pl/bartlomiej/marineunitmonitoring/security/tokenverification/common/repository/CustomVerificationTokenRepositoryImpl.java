package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.helper.repository.CustomRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenConstants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public class CustomVerificationTokenRepositoryImpl implements CustomVerificationTokenRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final CustomRepository customRepository;

    public CustomVerificationTokenRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate, CustomRepository customRepository) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.customRepository = customRepository;
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
        return customRepository.updateOne(id, VerificationTokenConstants.IS_VERIFIED, isVerified, VerificationToken.class)
                .then();
    }
}
