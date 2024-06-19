package pl.bartlomiej.marineunitmonitoring.user.service.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.sync.MongoUserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final MongoUserRepository mongoUserRepository;

    @Override
    public User getUser(String id) {
        return mongoUserRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    @Override
    public User createUser(User user) {
        if (mongoUserRepository.existsByEmail(user.getEmail()))
            throw new UniqueEmailException();
        // todo encode password
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