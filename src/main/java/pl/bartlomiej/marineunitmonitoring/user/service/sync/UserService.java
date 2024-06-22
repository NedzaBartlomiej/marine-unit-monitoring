package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import pl.bartlomiej.marineunitmonitoring.user.User;

public interface UserService {

    User getUserByOpenId(String openId);

    User createUser(User user);

    void deleteUser(String id);
}