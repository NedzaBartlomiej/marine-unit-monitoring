package pl.bartlomiej.marineunitmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomUserRepository customUserRepository;

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


    @Override
    public TrackedShip addTrackedShip(String id, Long mmsi) {
        if (!ActivePointsListHolder.isPointActive(mmsi)) {
            throw new MmsiConflictException("Invalid ship.");
        }
        TrackedShip trackedShip = new TrackedShip(mmsi, ActivePointsListHolder.getName(mmsi));
        return customUserRepository.pushTrackedShip(
                id,
                trackedShip
        );
    }
}