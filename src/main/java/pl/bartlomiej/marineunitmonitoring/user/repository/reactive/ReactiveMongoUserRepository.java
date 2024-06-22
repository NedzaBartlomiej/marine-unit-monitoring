package pl.bartlomiej.marineunitmonitoring.user.repository.reactive;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface ReactiveMongoUserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);

    Mono<User> findByOpenId(String openId);
}