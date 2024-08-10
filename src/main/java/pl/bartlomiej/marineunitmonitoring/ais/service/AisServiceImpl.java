package pl.bartlomiej.marineunitmonitoring.ais.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.ais.AisShip;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAuthTokenProvider;
import pl.bartlomiej.marineunitmonitoring.common.util.CommonShipFields;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.JWTConstants;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class AisServiceImpl implements AisService {

    private final AisApiAuthTokenProvider aisApiAuthTokenProvider;
    private final WebClient webClient;
    private final long resultLimit;
    private final String apiFetchLatestUri;
    private final String apiFetchByMmsiUri;

    public AisServiceImpl(AisApiAuthTokenProvider aisApiAuthTokenProvider,
                          WebClient webClient,
                          @Value("${project-properties.external-apis.ais-api.result-limit}") long resultLimit,
                          @Value("${ais-api.latest-ais-url}") String apiFetchLatestUri,
                          @Value("${ais-api.latest-ais-bymmsi-url}") String apiFetchByMmsiUri) {
        this.aisApiAuthTokenProvider = aisApiAuthTokenProvider;
        this.webClient = webClient;
        this.resultLimit = resultLimit;
        this.apiFetchLatestUri = apiFetchLatestUri;
        this.apiFetchByMmsiUri = apiFetchByMmsiUri;
    }

    @Override
    public Flux<AisShip> fetchLatestShips() {
        return aisApiAuthTokenProvider.getAisAuthToken()
                .flatMapMany(token -> webClient
                        .get()
                        .uri(apiFetchLatestUri)
                        .header(AUTHORIZATION, JWTConstants.BEARER_TYPE + token)
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
                        .header(AUTHORIZATION, JWTConstants.BEARER_TYPE + token)
                        .bodyValue(of(CommonShipFields.MMSI, identifiers.toArray()))
                        .retrieve()
                        .bodyToFlux(JsonNode.class)
                );
    }

}
