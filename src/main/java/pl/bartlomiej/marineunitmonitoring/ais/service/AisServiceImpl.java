package pl.bartlomiej.marineunitmonitoring.ais.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.bartlomiej.marineunitmonitoring.ais.Ais;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import pl.bartlomiej.marineunitmonitoring.geocode.service.GeocodeService;
import pl.bartlomiej.marineunitmonitoring.map.Point;

import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisServiceImpl implements AisService {
    public static final long RESULT_LIMIT = 30L;
    private static final int X_CORDS_INDEX = 0;
    private static final int Y_CORDS_INDEX = 1;
    private static final String AIS_API_URL = "https://live.ais.barentswatch.no/v1/latest/combined?modelType=Full&modelFormat=Geojson";
    private final GeocodeService geocodeService;
    private final AisApiAccessTokenService accessTokenService;
    private final RestTemplate restTemplate;

    @Cacheable("LatestAisPoints")
    public List<Point> getLatestAisPoints() {
        return ofNullable(this.getAisesFromApi().getBody())
                .stream()
                .flatMap(Arrays::stream)
                .limit(RESULT_LIMIT)
                .map(this::mapAisToPoint)
                .toList();
    }

    private Point mapAisToPoint(Ais ais) {
        return new Point(
                ais.properties().name() == null ? "UNKNOWN (not reported)" : ais.properties().name(),
                ais.geometry().coordinates().get(X_CORDS_INDEX),
                ais.geometry().coordinates().get(Y_CORDS_INDEX),
                ais.properties().destination() == null ? "UNKNOWN (not reported)" : ais.properties().destination(),
                geocodeService.getAddressCoords(ais.properties().destination()).lng(),
                geocodeService.getAddressCoords(ais.properties().destination()).lat()
        );
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION, "Bearer " + accessTokenService.getRefreshedToken());
        return httpHeaders;
    }

    private ResponseEntity<Ais[]> getAisesFromApi() {
        return restTemplate.exchange(
                AIS_API_URL,
                GET,
                new HttpEntity<>(getHttpHeaders()),
                Ais[].class
        );
    }
}
