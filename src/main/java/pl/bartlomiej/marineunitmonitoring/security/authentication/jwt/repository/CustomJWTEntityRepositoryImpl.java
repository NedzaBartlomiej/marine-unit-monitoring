package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.repository;

import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.helper.repository.CustomRepository;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTEntity;
import reactor.core.publisher.Mono;

@Repository
public class CustomJWTEntityRepositoryImpl implements CustomJWTEntityRepository {

    private final CustomRepository customRepository;

    public CustomJWTEntityRepositoryImpl(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    @Override
    public Mono<Void> updateIsValid(String id, boolean isValid) {
        return customRepository.updateOne(id, JWTConstants.IS_VALID, isValid, JWTEntity.class)
                .then();
    }

    @Override
    public Mono<Void> updateIsValidByUid(String uid, boolean isValid) {
        return customRepository.updateMulti(uid, JWTConstants.IS_VALID, isValid, JWTEntity.class)
                .then();
    }
}
