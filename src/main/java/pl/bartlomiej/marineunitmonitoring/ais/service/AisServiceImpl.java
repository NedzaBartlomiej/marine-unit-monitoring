package pl.bartlomiej.marineunitmonitoring.ais.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.Ais;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import pl.bartlomiej.marineunitmonitoring.geocode.service.GeocodeService;
import pl.bartlomiej.marineunitmonitoring.point.Point;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisServiceImpl implements AisService {
    public static final long RESULT_LIMIT = 3L;
    private static final int X_CORDS_INDEX = 0;
    private static final int Y_CORDS_INDEX = 1;
    private static final String AIS_API_URL = "https://live.ais.barentswatch.no/v1/latest/combined?modelType=Full&modelFormat=Geojson";
    private final GeocodeService geocodeService;
    private final AisApiAccessTokenService accessTokenService;
    private final WebClient webClient;

    public Flux<Point> getLatestAisPoints() {
        return this.getAisesFromApi()
                .take(RESULT_LIMIT)
                .flatMap(this::mapAisToPoint);
    }

    private Flux<Point> mapAisToPoint(Ais ais) {
        return geocodeService.getAddressCoords(ais.properties().destination())
                .map(position -> new Point(
                        ais.properties().name() == null ? "UNKNOWN (not reported)" : ais.properties().name(),
                        ais.geometry().coordinates().get(X_CORDS_INDEX),
                        ais.geometry().coordinates().get(Y_CORDS_INDEX),
                        ais.properties().destination() == null ? "UNKNOWN (not reported)" : ais.properties().destination(),
                        position.lng(),
                        position.lat()
                ));
    }

    private Flux<Ais> getAisesFromApi() {
        return accessTokenService.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .get()
                        .uri(AIS_API_URL)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(Ais.class)
                );
    }
}
