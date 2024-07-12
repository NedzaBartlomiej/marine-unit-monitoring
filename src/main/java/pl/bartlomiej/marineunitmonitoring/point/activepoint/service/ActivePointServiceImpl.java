package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.repository.MongoActivePointRepository;
import reactor.core.publisher.Mono;

import java.util.List;

import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.INVALID_SHIP;
import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
public class ActivePointServiceImpl implements ActivePointService {

    private final MongoActivePointRepository activePointRepository;
    private final AisService aisService;

    public ActivePointServiceImpl(MongoActivePointRepository activePointRepository, AisService aisService) {
        this.activePointRepository = activePointRepository;
        this.aisService = aisService;
    }

    @Override
    public Mono<List<Long>> getMmsis() {
        return activePointRepository.findAll()
                .switchIfEmpty(error(new MmsiConflictException("No active points found.")))
                .map(ActivePoint::getMmsi)
                .collectList();
    }

    @Override
    public Mono<Void> removeActivePoint(Long mmsi) {
        return this.isPointActive(mmsi)
                .flatMap(exists -> activePointRepository.deleteByMmsi(mmsi));
    }

    @Override
    public Mono<Void> addActivePoint(ActivePoint activePoint) {
        return activePointRepository.existsByMmsi(activePoint.getMmsi())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Point already exists.");
                        return empty();
                    } else {
                        return activePointRepository.save(activePoint).then();
                    }
                });
    }

    @EventListener(ApplicationReadyEvent.class)
    private Mono<Void> updateAfterAppStart() {
        log.info("Adding new active points after application start if exists.");
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

    @Override
    public Mono<Boolean> isPointActive(Long mmsi) {
        return activePointRepository.existsByMmsi(mmsi)
                .flatMap(exists -> exists
                        ? just(true)
                        : error(new MmsiConflictException(INVALID_SHIP.message)));
    }

    @Override
    public Mono<String> getName(Long mmsi) {
        return activePointRepository.getByMmsi(mmsi)
                .map(ActivePoint::getName)
                .switchIfEmpty(error(new MmsiConflictException(INVALID_SHIP.message)));
    }
}
