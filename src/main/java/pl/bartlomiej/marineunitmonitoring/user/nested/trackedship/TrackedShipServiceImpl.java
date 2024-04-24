package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePointsManager;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;

import java.util.List;

import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.INVALID_SHIP;
import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.SHIP_IS_ALREADY_TRACKED;

@Service
@RequiredArgsConstructor
public class TrackedShipServiceImpl implements TrackedShipService {

    private final MongoUserRepository mongoUserRepository;
    private final CustomUserRepository customUserRepository;

    public List<TrackedShip> getTrackedShips(String id) {
        List<TrackedShip> trackedShips = customUserRepository.getTrackedShips(id);
        if (trackedShips.isEmpty())
            throw new NoContentException();

        return trackedShips;
    }

    @Transactional
    @Override
    public TrackedShip addTrackedShip(String id, Long mmsi) {
        if (!mongoUserRepository.existsById(id))
            throw new NotFoundException();

        if (!ActivePointsManager.isPointActive(mmsi))
            throw new MmsiConflictException(INVALID_SHIP.message);

        if (this.isShipTracked(id, mmsi))
            throw new MmsiConflictException(SHIP_IS_ALREADY_TRACKED.message);

        TrackedShip trackedShip = new TrackedShip(mmsi, ActivePointsManager.getName(mmsi));
        return customUserRepository.pushTrackedShip(
                id,
                trackedShip
        );
    }

    @Transactional
    @Override
    public void removeTrackedShip(String id, Long mmsi) {
        if (!mongoUserRepository.existsById(id))
            throw new NotFoundException();

        if (!this.isShipTracked(id, mmsi))
            throw new NotFoundException();

        customUserRepository.pullTrackedShip(id, mmsi);
    }

    @Transactional
    @Override
    public void removeTrackedShip(Long mmsi) {

        if (!this.isShipTracked(mmsi))
            throw new NotFoundException();

        customUserRepository.pullTrackedShip(mmsi);
    }


    private Boolean isShipTracked(String id, Long mmsi) {
        return customUserRepository.getTrackedShips(id).stream()
                .anyMatch(trackedShip ->
                        trackedShip.getMmsi().equals(mmsi)
                );
    }

    private Boolean isShipTracked(Long mmsi) {
        return customUserRepository.getTrackedShips().stream()
                .anyMatch(trackedShip ->
                        trackedShip.getMmsi().equals(mmsi)
                );
    }
}
