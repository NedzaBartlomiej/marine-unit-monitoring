package pl.bartlomiej.marineunitmonitoring.security.emailverification.repository;

import pl.bartlomiej.marineunitmonitoring.security.emailverification.EmailVerificationEntity;
import reactor.core.publisher.Flux;

public interface CustomEmailVerificationEntityRepository {
    Flux<EmailVerificationEntity> findExpiredTokens();
}
