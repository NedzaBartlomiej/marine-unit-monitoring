package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShipRepository;
import pl.bartlomiej.marineunitmonitoring.common.error.MmsiConflictException;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static reactor.core.publisher.Mono.error;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private final ShipTrackHistoryRepository shipTrackHistoryRepository;
    private final TrackedShipRepository trackedShipRepository;
    private final WebClient webClient;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final AisApiAccessTokenService accessTokenService;
    @Value("${secrets.ais-api.latest-ais-bymmsi-url}")
    private String aisApiUrl;


    // TRACK HISTORY - operations

    @Override
    public Flux<ShipTrack> getShipTrackHistory() {
//        return reactiveMongoTemplate.changeStream(ShipTrack.class)
//                .filter(where("operationType").is(INSERT))
//                .listen()
//                .mapNotNull(ChangeStreamEvent::getBody)
//                .doOnError(error -> log.error("Something go wrong: {}", error.toString()))
//                .doOnNext(result -> log.info("New insert into collection: {}", result));
        return shipTrackHistoryRepository.findAll()
                .switchIfEmpty(error(new NoContentException()));
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60)
    public void saveTracksForTrackedShips() {
        this.fetchShipTracks()
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

    private Mono<Void> deleteShipTrackHistory(Long mmsi) {
        return shipTrackHistoryRepository.deleteShipTracksByMmsi(mmsi);
    }


    // TRACKED SHIPS - operations

    @Override
    public Mono<TrackedShip> saveTrackedShip(TrackedShip trackedShip) {
        return trackedShipRepository.findByMmsi(trackedShip.getMmsi())
                .hasElement()
                .flatMap(hasElement -> {
                    if (hasElement)
                        return error(new MmsiConflictException("The ship is already being tracked."));
                    if (!ActivePointsListHolder.isMmsiActive(trackedShip.getMmsi()))
                        return error(new MmsiConflictException("Invalid ship."));
                    log.info("Successfully saved ship: {}, to tracking list.", trackedShip.getMmsi());
                    return trackedShipRepository.save(trackedShip);
                });
    }


    // GET SHIP TRACKS TO SAVE - operations

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Mono<Void> deleteTrackedShip(Long mmsi) {
        return trackedShipRepository.findByMmsi(mmsi)
                .switchIfEmpty(
                        error(new MmsiConflictException("This ship is not tracked.")))
                .flatMap(trackedShip -> {
                    log.info("Successfully deleted ship: {}, from tracking list.", trackedShip.getMmsi());
                    return trackedShipRepository.delete(trackedShip);
                })
                .then(this.deleteShipTrackHistory(mmsi));
    }

    private Mono<List<TrackedShip>> fetchTrackedShips() {
        return trackedShipRepository.findAll()
                .switchIfEmpty(error(new NoContentException()))
                .collectList();
    }

    private Mono<List<Ship>> fetchShipsFromApi(List<Long> mmsis) {
        return accessTokenService.getAisAuthToken()
                .flatMap(token -> webClient
                        .post()
                        .uri(aisApiUrl)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .bodyValue(of("mmsi", mmsis))
                        .retrieve()
                        .bodyToMono(Ship[].class)
                        .flatMap(ships -> this.filterInvalidShips(ships, mmsis))
                );
    }

    private Mono<List<Ship>> filterInvalidShips(Ship[] ships, List<Long> mmsis) {
        List<Ship> validShips = Arrays.stream(ships)
                .filter(ship -> mmsis.contains(ship.mmsi()))
                .toList();

        List<Long> validMmsis = validShips.stream().map(Ship::mmsi).toList();
        mmsis.removeAll(validMmsis); // so making an invalid mmsi list
        mmsis.forEach(this::deleteTrackedShip);
        return Mono.just(validShips);
    }

    private Mono<List<ShipTrack>> mapToShipTracks(List<TrackedShip> trackedShips) {
        return this.fetchShipsFromApi(mapToMmsis(trackedShips))
                .flatMapMany(Flux::fromIterable)
                .map(ship ->
                        new ShipTrack(
                                ship.mmsi(),
                                ship.longitude(),
                                ship.latitude()
                        ))
                .collectList();
    }

    private List<Long> mapToMmsis(List<TrackedShip> trackedShips) {
        return trackedShips
                .stream()
                .map(TrackedShip::getMmsi)
                .toList();
    }

    private Mono<List<ShipTrack>> fetchShipTracks() {
        return this.fetchTrackedShips()
                .flatMap(this::mapToShipTracks);
    }

}