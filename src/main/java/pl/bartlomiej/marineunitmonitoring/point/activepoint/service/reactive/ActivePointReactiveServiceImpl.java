package pl.bartlomiej.marineunitmonitoring.point.activepoint.service.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.repository.ActivePointReactiveRepository;
import reactor.core.publisher.Mono;

import java.util.List;

import static reactor.core.publisher.Mono.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActivePointReactiveServiceImpl implements ActivePointReactiveService {

    private final ActivePointReactiveRepository activePointReactiveRepository;
    private final AisService aisService;

    // IMPLEMENTED/SUPPORTED ASYNC METHODS

    @Override
    public Mono<List<Long>> getMmsis() {
        return activePointReactiveRepository.findAll()
                .switchIfEmpty(error(new MmsiConflictException("No active points found.")))
                .map(ActivePoint::getMmsi)
                .collectList();
    }

    @Override
    public Mono<Void> removeActivePoint(Long mmsi) {
        return activePointReactiveRepository.existsByMmsi(mmsi)
                .flatMap(exists -> {
                    if (!exists) {
                        return error(new NotFoundException());
                    }
                    return activePointReactiveRepository.deleteByMmsi(mmsi);
                });
    }

    @Override
    public Mono<Void> addActivePoint(ActivePoint activePoint) {
        return activePointReactiveRepository.existsByMmsi(activePoint.getMmsi())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Point already exists.");
                        return empty();
                    } else {
                        return activePointReactiveRepository.save(activePoint).then();
                    }
                });
    }

    @EventListener(ApplicationReadyEvent.class)
    private Mono<Void> updateAfterAppStart() {
        log.info("Updating active points after application start.");
        return from(aisService.fetchLatestShips()
                .flatMap(aisShip -> this.addActivePoint(
                                new ActivePoint(
                                        aisShip.properties().mmsi(),
                                        aisShip.properties().name()
                                )
                        )
                )
        );
    }
}
