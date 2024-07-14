package pl.bartlomiej.marineunitmonitoring.ais;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAuthTokenProvider;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;

@Service
@Slf4j
public class AisServiceImpl implements AisService {

    private static final String BEARER = "Bearer ";
    private final AisApiAuthTokenProvider aisApiAuthTokenProvider;
    private final WebClient webClient;
    @Value("${project-properties.external-apis.ais-api.result-limit}")
    private long resultLimit;
    @Value("${secrets.ais-api.latest-ais-url}")
    private String apiFetchLatestUri;
    @Value("${secrets.ais-api.latest-ais-bymmsi-url}")
    private String apiFetchByMmsiUri;

    public AisServiceImpl(AisApiAuthTokenProvider aisApiAuthTokenProvider, WebClient webClient) {
        this.aisApiAuthTokenProvider = aisApiAuthTokenProvider;
        this.webClient = webClient;
    }

    @Override
    public Flux<AisShip> fetchLatestShips() {
        return aisApiAuthTokenProvider.getAisAuthToken()
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
    public Flux<JsonNode> fetchShipsByIdentifiers(List<String> identifiers) {
        return aisApiAuthTokenProvider.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .post()
                        .uri(apiFetchByMmsiUri)
                        .header(AUTHORIZATION, BEARER + token)
                        .bodyValue(of(MMSI.fieldName, identifiers.toArray()))
                        .retrieve()
                        .bodyToFlux(JsonNode.class)
                );
    }

}
