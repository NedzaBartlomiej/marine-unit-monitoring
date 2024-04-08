package pl.bartlomiej.marineunitmonitoring.user;

public interface UserService {


    User getUser(String id);

    User createUser(User user);

    void deleteUser(String id);
}