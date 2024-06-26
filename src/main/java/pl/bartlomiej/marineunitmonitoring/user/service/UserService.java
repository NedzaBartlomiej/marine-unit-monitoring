package pl.bartlomiej.marineunitmonitoring.user.service;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUserById(String id);

    Mono<User> createUser(User user);

    User createOrUpdateOAuth2BasedUser(String openId, String username, String email);

    Mono<Void> deleteUser(String id);

    Mono<Boolean> isUserExists(String id);
}