package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.sync.MongoUserRepository;

import static java.util.List.of;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.SIGNED;

@Service
public class UserServiceImpl implements UserService {

    private final MongoUserRepository mongoUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(MongoUserRepository mongoUserRepository, BCryptPasswordEncoder passwordEncoder) {
        this.mongoUserRepository = mongoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserByOpenId(String openId) {
        return mongoUserRepository.findByOpenId(openId)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    @Override
    public User createUser(User user) {
        if (mongoUserRepository.existsByEmail(user.getEmail()))
            throw new UniqueEmailException();
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return mongoUserRepository.save(user);
    }

    @Transactional
    @Override
    public User createOrUpdateOAuth2BasedUser(String openId, String username, String email) {
        return mongoUserRepository.findByOpenId(openId)
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
                );
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        User user = mongoUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mongoUserRepository.delete(user);
    }
}