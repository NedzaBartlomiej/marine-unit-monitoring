package pl.bartlomiej.marineunitmonitoring.ais.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.AisShip;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAuthTokenProvider;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static pl.bartlomiej.marineunitmonitoring.common.util.AppEntityField.MMSI;

@Service
public class AisServiceImpl implements AisService {

    private final AisApiAuthTokenProvider aisApiAuthTokenProvider;
    private final WebClient webClient;
    private final long resultLimit;
    private final String apiFetchLatestUri;
    private final String apiFetchByMmsiUri;
    private final String bearerPrefix;

    public AisServiceImpl(AisApiAuthTokenProvider aisApiAuthTokenProvider,
                          WebClient webClient,
                          @Value("${project-properties.external-apis.ais-api.result-limit}") long resultLimit,
                          @Value("${secrets.ais-api.latest-ais-url}") String apiFetchLatestUri,
                          @Value("${secrets.ais-api.latest-ais-bymmsi-url}") String apiFetchByMmsiUri,
                          @Value("${project-properties.security.token.bearer.type}") String bearerType) {
        this.aisApiAuthTokenProvider = aisApiAuthTokenProvider;
        this.webClient = webClient;
        this.resultLimit = resultLimit;
        this.apiFetchLatestUri = apiFetchLatestUri;
        this.apiFetchByMmsiUri = apiFetchByMmsiUri;
        this.bearerPrefix = bearerType + " ";
    }

    @Override
    public Flux<AisShip> fetchLatestShips() {
        return aisApiAuthTokenProvider.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .get()
                        .uri(apiFetchLatestUri)
                        .header(AUTHORIZATION, this.bearerPrefix + token)
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
                        .header(AUTHORIZATION, this.bearerPrefix + token)
                        .bodyValue(of(MMSI.fieldName, identifiers.toArray()))
                        .retrieve()
                        .bodyToFlux(JsonNode.class)
                );
    }

}
