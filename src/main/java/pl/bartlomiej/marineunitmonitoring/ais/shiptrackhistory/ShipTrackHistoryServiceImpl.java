package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShipRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipTrackHistoryServiceImpl implements ShipTrackHistoryService {

    private final ShipTrackHistoryRepository shipTrackHistoryRepository;
    private final TrackedShipRepository trackedShipRepository;
    private final WebClient webClient;
    private final AisApiAccessTokenService accessTokenService;
    @Value("${secrets.ais-api.latest-ais-bymmsi-url}")
    private String AIS_API_URL;


    // TRACK HISTORY - operations

    @Override
    public Mono<List<ShipTrack>> getShipTrackHistory() {
        return shipTrackHistoryRepository.findAll().collectList();
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60 * 5)
    public void saveTracksForTrackedShips() {
        this.getShipTracks()
                .flatMapIterable(shipTracks -> shipTracks)
                .flatMap(shipTrackHistoryRepository::save)
                .subscribe();
        log.info("Successfully saved ship track history.");
    }


    // TRACKED SHIPS - operations

    @Override
    public Mono<Void> saveTrackedShip(TrackedShip trackedShip) {
        log.info("Successfully saved ship: {}, to tracking list.", trackedShip.getMmsi());
        return trackedShipRepository.save(trackedShip).then();
    }

    @Override
    public Mono<Void> deleteTrackedShip(Long mmsi) {
        return trackedShipRepository.findByMmsi(mmsi)
                .flatMap(trackedShip -> {
                    log.info("Successfully deleted ship: {}, from tracking list.", trackedShip.getMmsi());
                    return trackedShipRepository.delete(trackedShip);
                });

    }

    private Flux<TrackedShip> getTrackedShips() {
        return trackedShipRepository.findAll();
    }


    // GET SHIP TRACKS TO SAVE - operations

    private Mono<Ship[]> getShipFromApi(Long mmsi) {
        return accessTokenService.getAisAuthToken()
                .flatMap(token -> webClient
                        .post()
                        .uri(AIS_API_URL)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .bodyValue(of("mmsi", List.of(mmsi)))
                        .retrieve()
                        .bodyToMono(Ship[].class)
                );
    }

    private Mono<ShipTrack> mapToShipTrack(TrackedShip trackedShip) {
        return this.getShipFromApi(trackedShip.getMmsi())
                .flatMapMany(Flux::fromArray)
                .map(ship ->
                        new ShipTrack(
                                ship.mmsi(),
                                ship.longitude(),
                                ship.latitude()
                        ))
                .next();
    }

    private Mono<List<ShipTrack>> getShipTracks() {
        return this.getTrackedShips()
                .flatMap(this::mapToShipTrack)
                .collectList();
    }

}
