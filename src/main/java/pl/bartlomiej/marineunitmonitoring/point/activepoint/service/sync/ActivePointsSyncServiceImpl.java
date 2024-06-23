package pl.bartlomiej.marineunitmonitoring.point.activepoint.service.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.repository.ActivePointRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActivePointsSyncServiceImpl implements ActivePointsSyncService {

    private final ActivePointRepository activePointRepository;

    // IMPLEMENTED/SUPPORTED SYNC METHODS

    @Override
    public String getName(Long mmsi) {
        return activePointRepository.findByMmsi(mmsi)
                .orElseThrow(NotFoundException::new)
                .getName();
    }

    @Override
    public Boolean isPointActive(Long mmsi) {
        return activePointRepository.existsByMmsi(mmsi);
    }
}
