package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import pl.bartlomiej.marineunitmonitoring.user.User;

public interface UserService {

    User getUserByOpenId(String openId);

    User createUser(User user);

    User createOrUpdateOAuth2BasedUser(String openId, String username, String email);

    void deleteUser(String id);
}