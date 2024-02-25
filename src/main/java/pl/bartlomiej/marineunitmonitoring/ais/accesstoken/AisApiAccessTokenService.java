package pl.bartlomiej.marineunitmonitoring.ais.accesstoken;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Service
@Slf4j
@RequiredArgsConstructor
public class AisApiAccessTokenService {

    private static final String CLIENT_ID = "bartek21122006@gmail.com:bartek21122006@gmail.com";
    private static final String SCOPE = "ais";
    private static final String CLIENT_SECRET = "Bartek2006bartek";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String ACCESS_TOKEN_API_URL = "https://id.barentswatch.no/connect/token";
    private final WebClient webClient;

    private static MultiValueMap<String, String> getAuthBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("scope", SCOPE);
        body.add("client_secret", CLIENT_SECRET);
        body.add("grant_type", GRANT_TYPE);
        return body;
    }

    @Cacheable(cacheNames = "AisAuthToken") // todo: add cache manager for ttl: 1hour
    public Mono<String> getAisAuthToken() {
        log.info("Access token has refreshed now.");
        return this.getAuthResponseFromApi()
                .map(this::getTokenFromApi)
                .cache();
    }

    private Mono<JsonNode> getAuthResponseFromApi() {
        return webClient
                .post()
                .uri(ACCESS_TOKEN_API_URL)
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .body(fromFormData(getAuthBody()))
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    private String getTokenFromApi(JsonNode response) {
        return response.get("access_token").asText();
    }
}
