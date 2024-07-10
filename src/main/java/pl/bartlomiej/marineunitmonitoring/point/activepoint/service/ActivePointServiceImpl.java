package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.repository.MongoActivePointRepository;
import reactor.core.publisher.Mono;

import java.util.List;

import static pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException.Message.INVALID_SHIP;
import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
public class ActivePointServiceImpl implements ActivePointService {

    private final MongoActivePointRepository mongoActivePointRepository;
    private final AisService aisService;

    public ActivePointServiceImpl(MongoActivePointRepository mongoActivePointRepository, AisService aisService) {
        this.mongoActivePointRepository = mongoActivePointRepository;
        this.aisService = aisService;
    }

    @Override
    public Mono<List<Long>> getMmsis() {
        return mongoActivePointRepository.findAll()
                .switchIfEmpty(error(new MmsiConflictException("No active points found.")))
                .map(ActivePoint::getMmsi)
                .collectList();
    }

    @Override
    public Mono<Void> removeActivePoint(Long mmsi) {
        return mongoActivePointRepository.existsByMmsi(mmsi)
                .flatMap(exists -> {
                    if (!exists) {
                        return error(new NotFoundException());
                    }
                    return mongoActivePointRepository.deleteByMmsi(mmsi);
                });
    }

    @Override
    public Mono<Void> addActivePoint(ActivePoint activePoint) {
        return mongoActivePointRepository.existsByMmsi(activePoint.getMmsi())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Point already exists.");
                        return empty();
                    } else {
                        return mongoActivePointRepository.save(activePoint).then();
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

    @Override
    public Mono<Void> isPointActive(Long mmsi) {
        return mongoActivePointRepository.existsByMmsi(mmsi)
                .flatMap(isActive -> {
                    if (!isActive) {
                        return Mono.error(new MmsiConflictException(INVALID_SHIP.message));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<String> getName(Long mmsi) {
        return mongoActivePointRepository.getByMmsi(mmsi)
                .map(ActivePoint::getName)
                .switchIfEmpty(error(new MmsiConflictException(INVALID_SHIP.message)));
    }
}
