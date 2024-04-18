package pl.bartlomiej.marineunitmonitoring.shiptracking;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.common.util.DateRange;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.INSERT;
import static pl.bartlomiej.marineunitmonitoring.common.config.MongoConfig.OPERATION_TYPE;
import static pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack.*;
import static reactor.core.publisher.Flux.just;
import static reactor.core.publisher.Mono.error;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private static final int TRACK_HISTORY_SAVE_DELAY = 1000 * 60 * 5;
    private final AisService aisService;
    private final ShipTrackHistoryRepository shipTrackHistoryRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;


    // TRACK HISTORY - operations

    //todo zwraca sie totalnie roznie i losowo jest jakis problem ze strumieniami chyba -- tutaj jest empty stream
    @Override
    public Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to) {

        // PROCESS DATE RANGE
        DateRange dateRange = this.processDateRange(new DateRange(from, to));

        // DB RESULT STREAM
        Flux<ShipTrack> dbStream = shipTrackHistoryRepository
                .findByReadingTimeBetweenAndMmsiIsIn(
                        dateRange.getFrom(), dateRange.getTo(), mmsis)
                .switchIfEmpty(error(NoContentException::new));

        // CHANGE STREAM
        Aggregation pipeline = newAggregation(match(
                        Criteria.where(OPERATION_TYPE).is(INSERT)
                                .and(MMSI).in(mmsis)
                                .and(READING_TIME).gte(dateRange.getFrom()).lte(dateRange.getTo())
                )
        );

        Flux<ChangeStreamEvent<ShipTrack>> changeStream = reactiveMongoTemplate.changeStream(
                SHIP_TRACK_HISTORY,
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
    }

    private DateRange processDateRange(DateRange dateRange) {

        final LocalDateTime ZERO_DATE = now(systemDefault());

        if (dateRange.getFrom() == null && dateRange.getTo() == null) {
            dateRange.setFrom(ZERO_DATE);
            dateRange.setTo(now(systemDefault()));
        } else if (dateRange.getFrom() == null) {
            dateRange.setFrom(ZERO_DATE);
        } else if (dateRange.getTo() == null) {
            dateRange.setTo(now(systemDefault()));
        }
        return dateRange;
    }

    @Scheduled(initialDelay = 0, fixedDelay = TRACK_HISTORY_SAVE_DELAY)
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMap(shipTrackHistoryRepository::save)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .doOnError(error -> log.error("Something go wrong on saving ship tracks - {}", error.getMessage()))
                .subscribe();
    }


    public Mono<Void> clearShipHistory(Long mmsi) {
        return shipTrackHistoryRepository.findByMmsi(mmsi)
                .flatMap(shipTrack -> {
                    if (shipTrack == null) {
                        log.error("Not found any ship tracks for ship: {}", mmsi);
                        return Mono.empty();
                    }
                    return shipTrackHistoryRepository.deleteAllByMmsi(mmsi);
                });
    }


    // GET SHIP TRACKS TO SAVE - operations

    private Flux<ShipTrack> getShipTracks() {
        return aisService.fetchShipsByIdentifiers(
                        this.getShipMmsisToTrack()
                )
                .switchIfEmpty(
                        error(NoContentException::new)
                )
                .flatMap(this::mapToShipTrack);
    }

    private List<Long> getShipMmsisToTrack() {
        return ActivePointsManager.getMmsis();
    }

    private Flux<ShipTrack> mapToShipTrack(JsonNode ship) {

        final String LONGITUDE = "longitude";
        final String LATITUDE = "latitude";

        return just(
                new ShipTrack(
                        ship.get(MMSI).asLong(),
                        ship.get(LONGITUDE).asDouble(),
                        ship.get(LATITUDE).asDouble()
                )
        );
    }

}