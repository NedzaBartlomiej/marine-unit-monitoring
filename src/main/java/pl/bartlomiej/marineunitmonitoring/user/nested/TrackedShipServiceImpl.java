package pl.bartlomiej.marineunitmonitoring.user.nested;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import pl.bartlomiej.marineunitmonitoring.shiptrackhistory.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.UserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;

import java.util.Collection;
import java.util.List;

import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.INVALID_SHIP;
import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.SHIP_IS_ALREADY_TRACKED;

@Service
@RequiredArgsConstructor
public class TrackedShipServiceImpl implements TrackedShipService {

    private final UserRepository userRepository;
    private final CustomUserRepository customUserRepository;
    private final ShipTrackHistoryService shipTrackHistoryService;

    public List<TrackedShip> getTrackedShips(String id) {
        List<TrackedShip> trackedShips = customUserRepository.getTrackedShips(id);
        if (trackedShips.isEmpty())
            throw new NoContentException();

        return trackedShips;
    }

    public List<TrackedShip> getTrackedShips() {
        List<TrackedShip> trackedShips = customUserRepository.getTrackedShips();
        if (trackedShips.isEmpty())
            throw new NoContentException();

        return trackedShips;
    }

    @Transactional
    @Override
    public TrackedShip addTrackedShip(String id, Long mmsi) {
        if (!userRepository.existsById(id))
            throw new NotFoundException();

        if (!ActivePointsListHolder.isPointActive(mmsi))
            throw new MmsiConflictException(INVALID_SHIP.toString());

        if (this.isShipTracked(id, mmsi))
            throw new MmsiConflictException(SHIP_IS_ALREADY_TRACKED.toString());

        TrackedShip trackedShip = new TrackedShip(mmsi, ActivePointsListHolder.getName(mmsi));
        return customUserRepository.pushTrackedShip(
                id,
                trackedShip
        );
    }

    @Transactional
    @Override
    public void removeTrackedShip(String id, Long mmsi) {
        if (!userRepository.existsById(id))
            throw new NotFoundException();

        if (!this.isShipTracked(id, mmsi))
            throw new NotFoundException();

        if (this.isTrackedShipUnused(mmsi))
            shipTrackHistoryService.clearShipHistory(mmsi).subscribe();

        customUserRepository.pullTrackedShip(id, mmsi);
    }

    @Transactional
    @Override
    public void removeTrackedShip(Long mmsi) {

        if (!this.isShipTracked(mmsi))
            throw new NotFoundException();

        if (this.isTrackedShipUnused(mmsi))
            shipTrackHistoryService.clearShipHistory(mmsi).subscribe();

        customUserRepository.pullTrackedShip(mmsi);
    }

    private Boolean isTrackedShipUnused(Long mmsi) {
        return userRepository.findAll().stream()
                .map(User::getTrackedShips)
                .flatMap(Collection::stream)
                .noneMatch(trackedShip ->
                        trackedShip.getMmsi().equals(mmsi)
                );
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
