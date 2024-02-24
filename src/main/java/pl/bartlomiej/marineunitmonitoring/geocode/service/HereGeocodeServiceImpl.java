package pl.bartlomiej.marineunitmonitoring.geocode.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.bartlomiej.marineunitmonitoring.geocode.Position;

import static org.springframework.http.HttpMethod.GET;

@Service
@Slf4j
@RequiredArgsConstructor
public class HereGeocodeServiceImpl implements GeocodeService {
    private static final String GEOCODE_API_KEY = "dcHxXlEttoO4p9UOC6mGknjJHIwQpcrMizZ9qecuLSc";
    private final RestTemplate restTemplate;

    @Cacheable(cacheNames = "AddressCoords")
    @Override
    public Position getAddressCoords(String address) {
        return this.getPositionFromResponse(this.getGeocodeFromApi(address), address);
    }

    @SneakyThrows
    private JsonNode getGeocodeFromApi(String address) {
        if (address == null) {
            log.error("Null address, skipping request sending.");
            return null;
        }
        ResponseEntity<JsonNode> exchange = restTemplate.exchange(
                getGeocodeApiUrl(address),
                GET,
                HttpEntity.EMPTY,
                JsonNode.class);
        log.info("Requesting geocode for address: {}", address);
        Thread.sleep(250);
        return exchange.getBody();
    }

    private Position getPositionFromResponse(JsonNode response, String address) {
        try {
            JsonNode position = response.get("items").get(0).get("position");
            return new Position(position.get("lat").asDouble(), position.get("lng").asDouble());
        } catch (NullPointerException e) {
            log.error("Geocode not found for: {}", address);
            return new Position(0, 0);
        }
    }

    private String getGeocodeApiUrl(String address) {
        return "https://geocode.search.hereapi.com/v1/geocode?q=" +
                address +
                "&apiKey=" + GEOCODE_API_KEY;
    }
}
