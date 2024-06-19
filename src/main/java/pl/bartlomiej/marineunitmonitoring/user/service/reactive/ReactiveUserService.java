package pl.bartlomiej.marineunitmonitoring.user.service.reactive;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface ReactiveUserService {

    Mono<User> getUserById(String id);

    Mono<User> getUserByEmail(String email);

    Mono<User> createUser(User user);

    Mono<Void> deleteUser(String id);
}
