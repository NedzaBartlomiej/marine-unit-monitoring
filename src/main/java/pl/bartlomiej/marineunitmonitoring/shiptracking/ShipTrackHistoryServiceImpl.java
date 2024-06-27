package pl.bartlomiej.marineunitmonitoring.shiptracking;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import pl.bartlomiej.marineunitmonitoring.shiptracking.helper.DateRangeHelper;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.CustomShipTrackHistoryRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.MongoShipTrackHistoryRepository;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.INSERT;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.OPERATION_TYPE;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.*;
import static reactor.core.publisher.Flux.just;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

@Service
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private static final int TRACK_HISTORY_SAVE_DELAY = 1000 * 60 * 5;
    private final AisService aisService;
    private final TrackedShipService trackedShipService;
    private final MongoShipTrackHistoryRepository mongoShipTrackHistoryRepository;
    private final CustomShipTrackHistoryRepository customShipTrackHistoryRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ActivePointService activePointService;

    public ShipTrackHistoryServiceImpl(
            AisService aisService, TrackedShipService trackedShipService,
            MongoShipTrackHistoryRepository mongoShipTrackHistoryRepository,
            CustomShipTrackHistoryRepository customShipTrackHistoryRepository,
            ReactiveMongoTemplate reactiveMongoTemplate,
            @Qualifier("activePointServiceImpl") ActivePointService activePointService) {
        this.aisService = aisService;
        this.trackedShipService = trackedShipService;
        this.mongoShipTrackHistoryRepository = mongoShipTrackHistoryRepository;
        this.customShipTrackHistoryRepository = customShipTrackHistoryRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.activePointService = activePointService;
    }


    // TRACK HISTORY - operations

    @Override
    public Flux<ShipTrack> getShipTrackHistory(String userId, LocalDateTime from, LocalDateTime to) {
        return trackedShipService.getTrackedShips(userId)
                .map(TrackedShip::getMmsi)
                .collectList()
                .flatMapMany(mmsis -> {

                    // PROCESS DATE RANGE
                    DateRangeHelper dateRangeHelper = new DateRangeHelper(from, to);

                    // DB RESULT STREAM
                    Flux<ShipTrack> dbStream = customShipTrackHistoryRepository
                            .findByMmsiInAndReadingTimeBetween(mmsis, dateRangeHelper.from(), dateRangeHelper.to())
                            .switchIfEmpty(error(NoContentException::new));

                    // CHANGE STREAM - used when the client wants to track the future
                    if (dateRangeHelper.to().isAfter(now()) || to == null) {

                        AggregationOperation match;
                        if (to == null) {
                            match = match(
                                    Criteria.where(OPERATION_TYPE).is(INSERT)
                                            .and(MMSI.fieldName).in(mmsis)
                            );
                        } else {
                            match = match(
                                    Criteria.where(OPERATION_TYPE).is(INSERT)
                                            .and(MMSI.fieldName).in(mmsis)
                                            .and(READING_TIME.fieldName).lte(dateRangeHelper.to())
                            );
                        }
                        Aggregation pipeline = newAggregation(match);

                        Flux<ChangeStreamEvent<ShipTrack>> changeStream = reactiveMongoTemplate.changeStream(
                                SHIP_TRACK_HISTORY.fieldName,
                                ChangeStreamOptions.builder()
                                        .filter(pipeline)
                                        .build(),
                                ShipTrack.class
                        );

                        Flux<ShipTrack> shipTrackStream = changeStream
                                .mapNotNull(ChangeStreamEvent::getBody)
                                .doOnNext(shipTrack ->
                                        log.info("New ShipTrack returning... mmsi: {}", shipTrack.getMmsi())
                                );
                        return dbStream.concatWith(shipTrackStream);
                    } else {
                        return dbStream;
                    }
                });
    }

    @Scheduled(initialDelay = 0, fixedDelay = TRACK_HISTORY_SAVE_DELAY)
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMap(this::saveNoStationaryTrack)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .doOnError(error -> log.error("Something go wrong on saving ship tracks - {}", error.getMessage()))
                .subscribe();
    }

    private Mono<Void> saveNoStationaryTrack(ShipTrack shipTrack) {
        return customShipTrackHistoryRepository.getLatest(shipTrack.getMmsi())
                .flatMap(lst -> {
                    if ((lst.getX().equals(shipTrack.getX()) && lst.getY().equals(shipTrack.getY()))) {
                        log.warn("The ship did not change its position - saving canceled");
                        return empty();
                    } else {
                        return mongoShipTrackHistoryRepository.save(shipTrack).then();
                    }
                })
                .onErrorResume(t -> mongoShipTrackHistoryRepository.save(shipTrack).then());
    }

    public Mono<Void> clearShipHistory(Long mmsi) {
        return mongoShipTrackHistoryRepository.existsByMmsi(mmsi)
                .flatMap(exists -> {
                    if (!exists) {
                        return error(new NotFoundException());
                    }
                    return mongoShipTrackHistoryRepository.deleteByMmsi(mmsi);
                });
    }

    // GET SHIP TRACKS TO SAVE - operations

    private Flux<ShipTrack> getShipTracks() {
        return this.getShipMmsisToTrack()
                .flatMapMany(aisService::fetchShipsByIdentifiers)
                .switchIfEmpty(
                        error(NoContentException::new)
                )
                .flatMap(this::mapToShipTrack);
    }

    private Mono<List<Long>> getShipMmsisToTrack() {
        return activePointService.getMmsis()
                .doOnError(error -> log.error("Something go wrong when getting mmsis to track - {}",
                        error.getMessage())
                );
    }

    private Flux<ShipTrack> mapToShipTrack(JsonNode ship) {

        final String LONGITUDE = "longitude";
        final String LATITUDE = "latitude";

        return just(
                new ShipTrack(
                        ship.get(MMSI.fieldName).asLong(),
                        ship.get(LONGITUDE).asDouble(),
                        ship.get(LATITUDE).asDouble()
                )
        );
    }

}