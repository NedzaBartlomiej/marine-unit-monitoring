package pl.bartlomiej.marineunitmonitoring.ais.accesstoken;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Service
@Slf4j
public class AisApiAccessTokenServiceImpl implements AisApiAccessTokenService {

    private static final String CLIENT_ID = "bartek21122006@gmail.com:bartek21122006@gmail.com";
    private static final String SCOPE = "ais";
    private static final String CLIENT_SECRET = "Bartek2006bartek";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String ACCESS_TOKEN_API_URL = "https://id.barentswatch.no/connect/token";
    RestTemplate restTemplate = new RestTemplate();

    private static MultiValueMap<String, String> getAuthBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("scope", SCOPE);
        body.add("client_secret", CLIENT_SECRET);
        body.add("grant_type", GRANT_TYPE);
        return body;
    }

    private static HttpHeaders getAuthRequestHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
        return httpHeaders;
    }

    @Cacheable("AisApiAuthToken")
    public String getRefreshedToken() {
        log.info("Access token has refreshed now.");
        return this.getTokenFromApi(this.getAuthResponseFromApi());
    }

    private JsonNode getAuthResponseFromApi() {
        ResponseEntity<JsonNode> exchange = restTemplate.exchange(
                ACCESS_TOKEN_API_URL,
                POST,
                new HttpEntity<>(getAuthBody(), getAuthRequestHeaders()),
                JsonNode.class);
        return exchange.getBody();
    }

    private String getTokenFromApi(JsonNode response) {
        return response.get("access_token").asText();
    }
}
