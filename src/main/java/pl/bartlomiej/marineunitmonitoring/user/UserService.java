package pl.bartlomiej.marineunitmonitoring.user;

import reactor.core.publisher.Mono;

public interface UserService {


    Mono<User> getUser(String id);

    Mono<User> createUser(User user);

    Mono<Void> deleteUser(String id);
}