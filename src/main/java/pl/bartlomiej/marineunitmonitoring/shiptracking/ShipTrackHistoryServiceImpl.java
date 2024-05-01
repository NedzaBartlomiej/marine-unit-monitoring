package pl.bartlomiej.marineunitmonitoring.shiptracking;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import pl.bartlomiej.marineunitmonitoring.shiptracking.helper.DateRangeHelper;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.CustomShipTrackHistoryRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.MongoShipTrackHistoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.INSERT;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.OPERATION_TYPE;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.*;
import static reactor.core.publisher.Flux.just;
import static reactor.core.publisher.Mono.error;

@Service
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private static final int TRACK_HISTORY_SAVE_DELAY = 1000 * 60 * 5;
    private static short trackCounter;
    private final AisService aisService;
    private final MongoShipTrackHistoryRepository mongoShipTrackHistoryRepository;
    private final CustomShipTrackHistoryRepository customShipTrackHistoryRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ActivePointService activePointService;

    public ShipTrackHistoryServiceImpl(
            AisService aisService,
            MongoShipTrackHistoryRepository mongoShipTrackHistoryRepository,
            CustomShipTrackHistoryRepository customShipTrackHistoryRepository,
            ReactiveMongoTemplate reactiveMongoTemplate,
            @Qualifier("activePointAsyncService") ActivePointService activePointService) {
        this.aisService = aisService;
        this.mongoShipTrackHistoryRepository = mongoShipTrackHistoryRepository;
        this.customShipTrackHistoryRepository = customShipTrackHistoryRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.activePointService = activePointService;
    }


    // TRACK HISTORY - operations

    @Override
    public Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to) {

        // PROCESS DATE RANGE
        DateRangeHelper dateRangeHelper = this.processDateRange(new DateRangeHelper(from, to));

        // DB RESULT STREAM
        Flux<ShipTrack> dbStream = customShipTrackHistoryRepository
                .findByMmsiInAndReadingTimeBetween(mmsis, dateRangeHelper.getFrom(), dateRangeHelper.getTo())
                .switchIfEmpty(error(NoContentException::new));

        // CHANGE STREAM - used when the client wants to track the future
        if (dateRangeHelper.getTo().isAfter(now()) || to == null) {
            Aggregation pipeline = newAggregation(match(
                            Criteria.where(OPERATION_TYPE).is(INSERT)
                                    .and(MMSI.fieldName).in(mmsis)
                                    .and(READING_TIME.fieldName).lte(dateRangeHelper.getTo())
                    )
            );

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

    }

    private DateRangeHelper processDateRange(DateRangeHelper dateRangeHelper) {

        final LocalDateTime ZERO_DATE = of(0, 1, 1, 0, 0, 0);

        log.info("Processing dateRange: {} - {}", dateRangeHelper.getFrom(), dateRangeHelper.getTo());
        if (dateRangeHelper.getFrom() == null) {
            dateRangeHelper.setFrom(ZERO_DATE);
        }
        if (dateRangeHelper.getTo() == null) {
            dateRangeHelper.setTo(now());
        }
        log.info("Processed dateRange: {} - {}", dateRangeHelper.getFrom(), dateRangeHelper.getTo());
        return dateRangeHelper;
    }

    @Scheduled(initialDelay = 0, fixedDelay = TRACK_HISTORY_SAVE_DELAY)
    public void saveTracksForTrackedShips() {

        // clearing not moving ship-track copies of one not moving ship-track
        if (trackCounter < 3) {
            trackCounter++;
        } else if (trackCounter == 3) {
            this.clearNotMovingTracks().subscribe();
            trackCounter = 0;
        }

        this.getShipTracks()
                .flatMap(mongoShipTrackHistoryRepository::save)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .doOnError(error -> log.error("Something go wrong on saving ship tracks - {}", error.getMessage()))
                .subscribe();
    }

    private Mono<Void> clearNotMovingTracks() {

    }


    public Mono<Void> clearShipHistory(Long mmsi) {
        return mongoShipTrackHistoryRepository.findByMmsi(mmsi)
                .flatMap(shipTrack -> {
                    if (shipTrack == null) {
                        log.error("Not found any ship tracks for ship: {}", mmsi);
                        return Mono.empty();
                    }
                    return mongoShipTrackHistoryRepository.deleteAllByMmsi(mmsi);
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