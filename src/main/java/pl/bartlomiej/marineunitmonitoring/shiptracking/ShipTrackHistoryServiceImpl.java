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
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShipService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public static final int TRACK_HISTORY_SAVE_DELAY = 1000 * 60;
    public static final String OPERATION_TYPE = "operationType";
    public static final String INSERT = "insert";
    private final AisService aisService;
    private final ShipTrackHistoryRepository shipTrackHistoryRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final TrackedShipService trackedShipService;


    // TRACK HISTORY - operations

    @Override
    public Flux<ShipTrack> getShipTrackHistory(List<Long> mmsis, LocalDateTime from, LocalDateTime to) {

        // PROCESS DATE RANGE
        DateRange dateRange = this.processDateRange(new DateRange(from, to));


        // DB RESULT STREAM
        Flux<ShipTrack> dbStream = shipTrackHistoryRepository
                .findByReadingTimeBetween(dateRange.getFrom(), dateRange.getTo());

        // CHANGE STREAM
        Aggregation pipeline = newAggregation(match(
                        Criteria.where(OPERATION_TYPE).is(INSERT)
                                .and(MMSI).in(mmsis)
                                .and(READING_TIME).gte(from).lte(to) // todo problem z konwersja bo tutaj mongo chce Date
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
        if (dateRange.getFrom() == null && dateRange.getTo() == null) {
            dateRange.setFrom(LocalDateTime.MIN);
            dateRange.setTo(LocalDateTime.MAX);
        } else if (dateRange.getFrom() == null) {
            dateRange.setFrom(LocalDateTime.MIN);
        } else if (dateRange.getTo() == null) {
            dateRange.setTo(LocalDateTime.MAX);
        }
        return dateRange;
    }

    @Scheduled(initialDelay = 0, fixedDelay = TRACK_HISTORY_SAVE_DELAY)
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMapIterable(shipTracks -> shipTracks)
                .flatMap(shipTrackHistoryRepository::save)
                .doOnComplete(() -> log.info("Successfully saved tracked ships coordinates."))
                .subscribe(
                        ignoredResult -> {
                        },
                        error -> log.error(
                                "Something go wrong when saving tracked ships coordinates: {}",
                                error.getMessage()));
    }

    private Mono<Void> clearShipHistory(Long mmsi) {
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

    private List<Long> getActiveShipMmsis() {
        return ActivePointsListHolder.getMmsis();
    }

    private Mono<List<JsonNode>> getShipsPositions(List<Long> mmsis) {
        return aisService.fetchShipsByMmsis(mmsis)
                .flatMap(ships ->
                        this.filterInvalidShips(ships, mmsis)
                );
    }

    private Mono<List<ShipTrack>> getShipTracks() {
        return this.getShipsPositions(
                        this.getActiveShipMmsis())
                .switchIfEmpty(
                        error(NoContentException::new))
                .flatMap(this::mapToShipTracks);
    }

    // todo maybe some refactor
    private Mono<List<JsonNode>> filterInvalidShips(List<JsonNode> ships, List<Long> mmsis) {
        List<JsonNode> validShips = ships.stream()
                .filter(Objects::nonNull)
                .toList();
        List<Long> validMmsis = validShips.stream()
                .map(jsonNode ->
                        jsonNode.get(MMSI).asLong()
                )
                .toList();
        List<Long> invalidMmsis = new ArrayList<>(mmsis);

        invalidMmsis.removeAll(validMmsis);
        invalidMmsis.forEach(mmsi -> {
            this.clearShipHistory(mmsi).subscribe();
            trackedShipService.removeTrackedShip(mmsi);
            ActivePointsListHolder.removeActivePoint(mmsi);
        });

        if (!invalidMmsis.isEmpty())
            log.error("Some ships were removed from the tracking list," +
                    " because they are not currently registered: {}", invalidMmsis);

        return just(validShips);
    }

    private Mono<List<ShipTrack>> mapToShipTracks(List<JsonNode> ships) {
        return just(
                ships.stream()
                        .map(ship ->
                                new ShipTrack(
                                        ship.get(MMSI).asLong(),
                                        ship.get(X).asDouble(),
                                        ship.get(Y).asDouble()
                                )
                        )
                        .toList()
        );
    }

}