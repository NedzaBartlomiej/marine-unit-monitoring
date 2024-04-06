package pl.bartlomiej.marineunitmonitoring.user.repository;

import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

public interface CustomUserRepository {

    TrackedShip pushTrackedShip(String id, TrackedShip trackedShip);
}
