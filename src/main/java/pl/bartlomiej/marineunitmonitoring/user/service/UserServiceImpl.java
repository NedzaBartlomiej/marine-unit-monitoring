package pl.bartlomiej.marineunitmonitoring.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.SyncMongoUserRepository;
import reactor.core.publisher.Mono;

import static java.util.List.of;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.SIGNED;
import static reactor.core.publisher.Mono.error;

@Service
public class UserServiceImpl implements UserService {

    private final MongoUserRepository mongoUserRepository;
    private final SyncMongoUserRepository syncMongoUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(MongoUserRepository mongoUserRepository, SyncMongoUserRepository syncMongoUserRepository, BCryptPasswordEncoder passwordEncoder) {
        this.mongoUserRepository = mongoUserRepository;
        this.syncMongoUserRepository = syncMongoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> getUserById(String id) {
        return mongoUserRepository.findById(id)
                .switchIfEmpty(error(NotFoundException::new));
    }

    @Transactional
    @Override
    public Mono<User> createUser(User user) { // todo - implement
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return null;
    }

    @Transactional
    @Override
    public User createOrUpdateOAuth2BasedUser(String openId, String username, String email) {
        return syncMongoUserRepository.findById(openId)
                .map(user -> {
                    user.setUsername(username);
                    user.setEmail(email);
                    return syncMongoUserRepository.save(user);
                })
                .orElseGet(() -> syncMongoUserRepository.save(
                                new User(
                                        openId,
                                        username,
                                        email,
                                        of(SIGNED)
                                )
                        )
                );
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(String id) {
        return mongoUserRepository.findById(id)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(mongoUserRepository::delete);
    }

    public Mono<Boolean> isUserExists(String id) {
        return mongoUserRepository.findById(id)
                .map(user -> true)
                .switchIfEmpty(Mono.error(NotFoundException::new));
    }
}