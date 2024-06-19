package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface UserService {


    User getUser(String id);

    User createUser(User user);

    void deleteUser(String id);
}