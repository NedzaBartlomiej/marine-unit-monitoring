package pl.bartlomiej.marineunitmonitoring.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.SyncMongoUserRepository;
import reactor.core.publisher.Mono;

import static java.util.List.of;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.SIGNED;
import static reactor.core.publisher.Mono.error;

@Slf4j
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
    public Mono<User> createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(of(SIGNED));
        return mongoUserRepository.save(user)
                .onErrorResume(throwable -> {
                    if (throwable instanceof DuplicateKeyException) {
                        log.error(UniqueEmailException.MESSAGE);
                        return error(UniqueEmailException::new);
                    } else {
                        return error(throwable);
                    }
                });
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