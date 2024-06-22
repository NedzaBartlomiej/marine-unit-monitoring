package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.sync.MongoUserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final MongoUserRepository mongoUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(MongoUserRepository mongoUserRepository, PasswordEncoder passwordEncoder) {
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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return mongoUserRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        User user = mongoUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mongoUserRepository.delete(user);
    }
}