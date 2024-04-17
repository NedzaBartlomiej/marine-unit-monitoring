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
import java.util.Objects;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack.*;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private static final String OPERATION_TYPE = "operationType";
    private static final String INSERT = "insert";
    private static final int TRACK_HISTORY_SAVE_DELAY = 1000 * 60 * 5;
    private final AisService aisService;
    private final ShipTrackHistoryRepository shipTrackHistoryRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;


    // TRACK HISTORY - operations

    @Override //todo zwraca sie totalnie roznie i losowo jest jakis problem ze strumieniami chyba
    public Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to) {

        // PROCESS DATE RANGE
        DateRange dateRange = this.processDateRange(new DateRange(from, to));

        // DB RESULT STREAM
        Flux<ShipTrack> dbStream = shipTrackHistoryRepository
                .findByReadingTimeBetweenAndMmsiIsIn(
                        dateRange.getFrom(), dateRange.getTo(), mmsis);

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

        final LocalDateTime ZERO_DATE = LocalDateTime.of(0, 1, 1, 0, 0);

        if (dateRange.getFrom() == null && dateRange.getTo() == null) {
            dateRange.setFrom(ZERO_DATE);
            dateRange.setTo(LocalDateTime.now());
        } else if (dateRange.getFrom() == null) {
            dateRange.setFrom(ZERO_DATE);
        } else if (dateRange.getTo() == null) {
            dateRange.setTo(LocalDateTime.now());
        }
        return dateRange;
    }

    @Scheduled(initialDelay = 0, fixedDelay = TRACK_HISTORY_SAVE_DELAY)
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMapIterable(shipTracks -> shipTracks)
                .flatMap(shipTrackHistoryRepository::save)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .doOnError(error -> log.error("Something go wrong on saving ship tracks - {}", error.getMessage()))
                .subscribe();
    }


    public Mono<Void> clearShipHistory(Long mmsi) {
        return shipTrackHistoryRepository.findByMmsi(mmsi)
                .flatMap(shipTrack -> {
                    if (shipTrack == null) {
                        log.info("Not found any ship tracks for ship: {}", mmsi);
                        return Mono.empty();
                    }
                    return shipTrackHistoryRepository.deleteAllByMmsi(mmsi);
                });
    }


    // GET SHIP TRACKS TO SAVE - operations

    private Mono<List<ShipTrack>> getShipTracks() {
        return aisService.fetchShipsByIdentifiers(
                        this.getShipMmsisToTrack()
                )
                .switchIfEmpty(
                        error(NoContentException::new)
                )
                .flatMap(this::mapToShipTracks);
    }

    private List<Long> getShipMmsisToTrack() {
        return ActivePointsManager.getMmsis();
    }

    private Mono<List<ShipTrack>> mapToShipTracks(List<JsonNode> ships) {

        final String LONGITUDE = "longitude";
        final String LATITUDE = "latitude";

        return just(
                ships.stream()
                        .filter(Objects::nonNull)
                        .map(ship ->
                                new ShipTrack(
                                        ship.get(MMSI).asLong(),
                                        ship.get(LONGITUDE).asDouble(),
                                        ship.get(LATITUDE).asDouble()
                                )
                        )
                        .toList()
        );
    }

}