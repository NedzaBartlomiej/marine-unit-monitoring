package pl.bartlomiej.marineunitmonitoring.ais;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.geocode.service.GeocodeService;
import pl.bartlomiej.marineunitmonitoring.point.ActivePointsListHolder;
import pl.bartlomiej.marineunitmonitoring.point.Point;
import reactor.core.publisher.Flux;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.bartlomiej.marineunitmonitoring.ais.Geometry.X_CORDS_INDEX;
import static pl.bartlomiej.marineunitmonitoring.ais.Geometry.Y_CORDS_INDEX;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisServiceImpl implements AisService {
    public static final long RESULT_LIMIT = 3L;
    private final GeocodeService geocodeService;
    private final AisApiAccessTokenService accessTokenService;
    private final WebClient webClient;
    @Value("${secrets.ais-api.latest-ais-url}")
    private String aisApiUrl;

    @Override
    public Flux<Point> getLatestAisPoints() {
        return this.fetchAisFromApi()
                .switchIfEmpty(Flux.error(new NoContentException()))
                .take(RESULT_LIMIT)
                .flatMap(this::mapAisToPoint);
    }

    private Flux<Point> mapAisToPoint(Ais ais) {
        return geocodeService.getAddressCoordinates(ais.properties().destination())
                .map(position -> {
                    ActivePointsListHolder.addActivePointMmsi(ais.properties().mmsi());
                    return new Point(
                            ais.properties().mmsi(),
                            ais.properties().name() == null ? "UNKNOWN (not reported)" : ais.properties().name(),
                            ais.geometry().coordinates().get(X_CORDS_INDEX),
                            ais.geometry().coordinates().get(Y_CORDS_INDEX),
                            ais.properties().destination() == null ? "UNKNOWN (not reported)" : ais.properties().destination(),
                            position.x(),
                            position.y()
                    );
                });
    }

    private Flux<Ais> fetchAisFromApi() {
        return accessTokenService.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .get()
                        .uri(aisApiUrl)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(Ais.class)
                );
    }
}
