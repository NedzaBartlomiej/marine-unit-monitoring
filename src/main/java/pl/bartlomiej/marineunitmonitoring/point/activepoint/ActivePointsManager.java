package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackHistoryService;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActivePointsManager {

    private final ActivePointRepository activePointRepository;
    private final TrackedShipService trackedShipService;
    private final ShipTrackHistoryService shipTrackHistoryService;

    public List<Long> getMmsis(boolean shouldThrowIfEmpty) {
        List<Long> mmsis = activePointRepository.findAll().stream()
                .map(ActivePoint::getMmsi)
                .toList();
        if (mmsis.isEmpty() && shouldThrowIfEmpty) {
            log.warn("No active points found.");
            throw new NoContentException();
        }
        return mmsis;
    }

    public String getName(Long mmsi) {
        return activePointRepository.findByMmsi(mmsi)
                .orElseThrow(NotFoundException::new)
                .getName();
    }

    public Boolean isPointActive(Long mmsi) {
        return activePointRepository.existsByMmsi(mmsi);
    }

    public void removeActivePoint(Long mmsi) {
        if (!activePointRepository.existsByMmsi(mmsi))
            throw new NotFoundException();
        activePointRepository.deleteByMmsi(mmsi);
    }

    public void addActivePoint(ActivePoint activePoint) {
        if (activePointRepository.existsByMmsi(activePoint.getMmsi()))
            throw new MmsiConflictException("Point is already on list.");
        activePointRepository.save(activePoint);
    }

    // todo refactor
    public void filterInactiveShips(List<Long> activeMmsis) {
        if (!getMmsis(false).isEmpty() && !activeMmsis.isEmpty()) {
            List<Long> actualMmsis = new ArrayList<>(getMmsis(false));
            List<Long> inactiveMmsis = activeMmsis.stream()
                    .filter(activeMmsi -> !actualMmsis.contains(activeMmsi))
                    .toList();

            if (inactiveMmsis.isEmpty()) {
                log.info("All points are active.");
            } else {
                inactiveMmsis
                        .forEach(mmsi -> {
                            log.info("Removing inactive point - {}", mmsi);
                            removeActivePoint(mmsi);
                            trackedShipService.removeTrackedShip(mmsi);
                            shipTrackHistoryService.clearShipHistory(mmsi).subscribe();
                        });
            }
        } else {
            log.info("No points to filter.");
        }
    }
}
