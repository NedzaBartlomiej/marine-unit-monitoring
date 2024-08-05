package pl.bartlomiej.marineunitmonitoring.user.service;

import pl.bartlomiej.marineunitmonitoring.user.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUser(String id);

    Mono<User> getUserByEmail(String email);

    Mono<User> createUser(User user, String ipAddress);

    Mono<Void> verifyUser(String id);

    Mono<Void> unlockUser(String id);

    Mono<Void> blockUser(String id);

    Mono<User> processAuthenticationFlowUser(String id, String username, String email, String tokenIssuer);

    Mono<Void> deleteUser(String id);

    Mono<Boolean> isUserExists(String id);

    Mono<String> identifyUser(String subjectId);

    Mono<Void> updatePassword(String id, String newPassword);

    Mono<Void> updateIsTwoFactorAuthEnabled(String id, Boolean isTwoFactorAuthEnabled);

    Mono<Void> trustIpAddress(String id, String ipAddress);
}