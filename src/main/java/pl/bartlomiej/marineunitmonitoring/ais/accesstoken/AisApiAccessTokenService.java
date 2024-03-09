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
    private String clientId;
    @Value("${secrets.ais-api.auth.scope}")
    private String scope;
    @Value("${secrets.ais-api.auth.client-secret}")
    private String clientSecret;
    @Value("${secrets.ais-api.auth.grant-type}")
    private String grantType;
    @Value("${secrets.ais-api.auth.url}")
    private String accessTokenApiUrl;

    private MultiValueMap<String, String> buildAuthBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("scope", scope);
        body.add("client_secret", clientSecret);
        body.add("grant_type", grantType);
        return body;
    }

    @Cacheable(cacheNames = AIS_AUTH_TOKEN_CACHE_NAME)
    public Mono<String> getAisAuthToken() {
        log.info("Access token has refreshed now.");
        return this.fetchAuthTokenFromApi()
                .map(this::extractTokenFromApiResponse)
                .cache();
    }

    public Mono<String> getAisAuthTokenWithoutCache() {
        return this.fetchAuthTokenFromApi()
                .map(this::extractTokenFromApiResponse);
    }

    private Mono<JsonNode> fetchAuthTokenFromApi() {
        return webClient
                .post()
                .uri(accessTokenApiUrl)
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .body(fromFormData(buildAuthBody()))
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    private String extractTokenFromApiResponse(JsonNode response) {
        return response.get("access_token").asText();
    }
}