package pl.bartlomiej.marineunitmonitoring.ais.accesstoken;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static pl.bartlomiej.marineunitmonitoring.common.config.RedisCacheConfig.AIS_AUTH_TOKEN_CACHE_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class AisApiAccessTokenService {

    private final WebClient webClient;
    @Value("${secrets.ais-api.auth.client-id}")
    private String CLIENT_ID;
    @Value("${secrets.ais-api.auth.scope}")
    private String SCOPE;
    @Value("${secrets.ais-api.auth.client-secret}")
    private String CLIENT_SECRET;
    @Value("${secrets.ais-api.auth.grant-type}")
    private String GRANT_TYPE;
    @Value("${secrets.ais-api.auth.url}")
    private String ACCESS_TOKEN_API_URL;

    private MultiValueMap<String, String> getAuthBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("scope", SCOPE);
        body.add("client_secret", CLIENT_SECRET);
        body.add("grant_type", GRANT_TYPE);
        return body;
    }

    @Cacheable(cacheNames = AIS_AUTH_TOKEN_CACHE_NAME)
    public Mono<String> getAisAuthToken() {
        log.info("Access token has refreshed now.");
        return this.getAuthResponseFromApi()
                .map(this::getTokenFromApi)
                .cache();
    }

    public Mono<String> getAisAuthTokenWithoutCache() {
        return this.getAuthResponseFromApi()
                .map(this::getTokenFromApi);
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