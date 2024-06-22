package pl.bartlomiej.marineunitmonitoring.user.service.reactive;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface ReactiveUserService {

    Mono<User> getUserByOpenId(String openId);

    Mono<User> createUser(User user);
}
