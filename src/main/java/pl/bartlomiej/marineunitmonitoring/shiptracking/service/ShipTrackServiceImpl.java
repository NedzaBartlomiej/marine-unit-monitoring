package pl.bartlomiej.marineunitmonitoring.shiptracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.service.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack;
import pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrackConstants;
import pl.bartlomiej.marineunitmonitoring.shiptracking.helper.DateRangeHelper;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.CustomShipTrackRepository;
import pl.bartlomiej.marineunitmonitoring.shiptracking.repository.MongoShipTrackRepository;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.service.TrackedShipService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.INSERT;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.OPERATION_TYPE;
import static reactor.core.publisher.Flux.just;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

@Service
public class ShipTrackServiceImpl implements ShipTrackService {

    private static final Logger log = LoggerFactory.getLogger(ShipTrackServiceImpl.class);
    private final AisService aisService;
    private final TrackedShipService trackedShipService;
    private final MongoShipTrackRepository mongoShipTrackRepository;
    private final CustomShipTrackRepository customShipTrackRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ActivePointService activePointService;

    public ShipTrackServiceImpl(
            AisService aisService, TrackedShipService trackedShipService,
            MongoShipTrackRepository mongoShipTrackRepository,
            CustomShipTrackRepository customShipTrackRepository,
            ReactiveMongoTemplate reactiveMongoTemplate,
            ActivePointService activePointService) {
        this.aisService = aisService;
        this.trackedShipService = trackedShipService;
        this.mongoShipTrackRepository = mongoShipTrackRepository;
        this.customShipTrackRepository = customShipTrackRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.activePointService = activePointService;
    }


    // TRACK HISTORY - operations

    @Override
    public Flux<ShipTrack> getShipTrackHistory(String userId, LocalDateTime from, LocalDateTime to) {
        return trackedShipService.getTrackedShips(userId)
                .map(TrackedShip::mmsi)
                .collectList()
                .flatMapMany(mmsis -> {

                    // PROCESS DATE RANGE
                    DateRangeHelper dateRangeHelper = new DateRangeHelper(from, to);

                    // DB RESULT STREAM
                    Flux<ShipTrack> dbStream = customShipTrackRepository
                            .findByMmsiInAndReadingTimeBetween(mmsis, dateRangeHelper.from(), dateRangeHelper.to())
                            .switchIfEmpty(error(NoContentException::new));

                    // CHANGE STREAM - used when the client wants to track the future
                    if (dateRangeHelper.to().isAfter(now()) || to == null) {

                        AggregationOperation match;
                        if (to == null) {
                            match = match(
                                    Criteria.where(OPERATION_TYPE).is(INSERT)
                                            .and(ShipTrackConstants.MMSI).in(mmsis)
                            );
                        } else {
                            match = match(
                                    Criteria.where(OPERATION_TYPE).is(INSERT)
                                            .and(ShipTrackConstants.MMSI).in(mmsis)
                                            .and(ShipTrackConstants.READING_TIME).lte(dateRangeHelper.to())
                            );
                        }
                        Aggregation pipeline = newAggregation(match);

                        Flux<ChangeStreamEvent<ShipTrack>> changeStream = reactiveMongoTemplate.changeStream(
                                ShipTrackConstants.SHIP_TRACKS_COLLECTION,
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

    @Scheduled(initialDelay = 0, fixedDelayString = "${project-properties.scheduling-delays.in-ms.ship-tracking.saving}")
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMap(this::saveNoStationaryTrack)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .doOnError(error -> log.error("Something go wrong on saving ship tracks - {}", error.getMessage()))
                .subscribe();
    }

    private Mono<Void> saveNoStationaryTrack(ShipTrack shipTrack) {
        return customShipTrackRepository.getLatest(shipTrack.getMmsi())
                .flatMap(lst -> {
                    if ((lst.getX().equals(shipTrack.getX()) && lst.getY().equals(shipTrack.getY()))) {
                        log.warn("The ship did not change its position - saving canceled");
                        return empty();
                    } else {
                        return mongoShipTrackRepository.save(shipTrack).then();
                    }
                })
                .onErrorResume(t -> mongoShipTrackRepository.save(shipTrack).then());
    }

    public Mono<Void> clearShipHistory(String mmsi) {
        return mongoShipTrackRepository.existsById(mmsi)
                .flatMap(exists -> {
                    if (!exists) {
                        return error(new NotFoundException());
                    }
                    return mongoShipTrackRepository.deleteById(mmsi);
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

    private Mono<List<String>> getShipMmsisToTrack() {
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
                        ship.get(ShipTrackConstants.MMSI).asText(),
                        ship.get(LONGITUDE).asDouble(),
                        ship.get(LATITUDE).asDouble()
                )
        );
    }

}