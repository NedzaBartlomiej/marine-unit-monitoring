package pl.bartlomiej.marineunitmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new UniqueEmailException();
        // todo encode password
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        userRepository.delete(user);
    }
}