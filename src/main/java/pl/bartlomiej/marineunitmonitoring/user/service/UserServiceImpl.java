package pl.bartlomiej.marineunitmonitoring.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static java.util.List.of;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.SIGNED;
import static reactor.core.publisher.Mono.error;

@Service
public class UserServiceImpl implements UserService {

    private final MongoUserRepository mongoUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(MongoUserRepository mongoUserRepository, BCryptPasswordEncoder passwordEncoder) {
        this.mongoUserRepository = mongoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> getUserByOpenId(String openId) {
        return mongoUserRepository.findById(openId)
                .switchIfEmpty(error(NotFoundException::new));
    }

    @Transactional
    @Override
    public Mono<User> createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return mongoUserRepository.findByEmail(user.getEmail())
                .flatMap(u -> mongoUserRepository.save(user))
                .switchIfEmpty(error(UniqueEmailException::new));
    }

    @Transactional
    @Override
    public User createOrUpdateOAuth2BasedUser(String openId, String username, String email) {
        return Optional.ofNullable(
                        mongoUserRepository.findById(openId).block()
                )
                .map(user -> {
                    user.setUsername(username);
                    user.setEmail(email);
                    return mongoUserRepository.save(user);
                })
                .orElseGet(() -> mongoUserRepository.save(
                                new User(
                                        openId,
                                        username,
                                        email,
                                        of(SIGNED)
                                )
                        )
                ).block();
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(String id) {
        return mongoUserRepository.findById(id)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(mongoUserRepository::delete);
    }
}