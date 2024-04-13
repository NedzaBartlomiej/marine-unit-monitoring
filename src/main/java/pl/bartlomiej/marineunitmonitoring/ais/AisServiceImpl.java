package pl.bartlomiej.marineunitmonitoring.ais;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.bartlomiej.marineunitmonitoring.shiptracking.ShipTrack.MMSI;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisServiceImpl implements AisService {

    private static final String BEARER = "Bearer ";
    private static final long resultLimit = 50;
    private final AisApiAccessTokenService accessTokenService;
    private final WebClient webClient;
    @Value("${secrets.ais-api.latest-ais-url}")
    private String apiFetchLatestUri;
    @Value("${secrets.ais-api.latest-ais-bymmsi-url}")
    private String apiFetchByMmsiUri;

    @Override
    public Flux<AisShip> fetchLatestShips() {
        return accessTokenService.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .get()
                        .uri(apiFetchLatestUri)
                        .header(AUTHORIZATION, BEARER + token)
                        .retrieve()
                        .bodyToFlux(AisShip.class)
                        .take(resultLimit)
                );
    }

    @Override
    public Mono<List<JsonNode>> fetchShipsByMmsis(List<Long> mmsis) {
        return accessTokenService.getAisAuthToken()
                .flatMap(token -> webClient
                        .post()
                        .uri(apiFetchByMmsiUri)
                        .header(AUTHORIZATION, BEARER + token)
                        .bodyValue(of(MMSI, mmsis))
                        .retrieve()
                        .bodyToMono(JsonNode[].class)
                        .map(jsonNodes -> stream(jsonNodes).toList())
                );
    }

}
