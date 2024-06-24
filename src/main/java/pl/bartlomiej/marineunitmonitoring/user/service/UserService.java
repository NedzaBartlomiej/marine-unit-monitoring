package pl.bartlomiej.marineunitmonitoring.user.service;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUserByOpenId(String openId);

    Mono<User> createUser(User user);

    User createOrUpdateOAuth2BasedUser(String openId, String username, String email);

    Mono<Void> deleteUser(String id);
}