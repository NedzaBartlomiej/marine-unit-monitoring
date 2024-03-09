package pl.bartlomiej.marineunitmonitoring.geocode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.geocode.Position;
import reactor.core.publisher.Flux;

import static pl.bartlomiej.marineunitmonitoring.common.config.RedisCacheConfig.ADDRESS_COORDS_CACHE_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class HereGeocodeServiceImpl implements GeocodeService {

    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final int FIRST_GEOCODE_SUGGESTION = 0;
    private final WebClient webClient;
    @Value("${secrets.geocode-api.api-key}")
    private String geocodeApiKey;

    @Cacheable(cacheNames = ADDRESS_COORDS_CACHE_NAME)
    public Flux<Position> getAddressCoordinates(String address) {
        return this.retrieveGeocodeFromApi(address)
                .map(response -> this.extractPositionFromResponse(response, address))
                .cache();
    }

    @NonNull
    private Flux<JsonNode> retrieveGeocodeFromApi(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.error("Null address, skipping request sending.");
            return Flux.just(this.createDefaultPositionNode());
        }
        return webClient
                .get()
                .uri(this.buildGeocodeApiUrl(address))
                .retrieve()
                .bodyToFlux(JsonNode.class);
    }

    private Position extractPositionFromResponse(JsonNode response, String address) {
        try {
            JsonNode position = response.get("items").get(FIRST_GEOCODE_SUGGESTION).get("position");
            return new Position(position.get(LNG).asDouble(), position.get(LAT).asDouble());
        } catch (NullPointerException e) {
            log.error("Geocode not found for: {}", address);
            return new Position(0.0, 0.0);
        }
    }

    private String buildGeocodeApiUrl(String address) {
        return "https://geocode.search.hereapi.com/v1/geocode?q=" +
                address +
                "&apiKey=" + geocodeApiKey;
    }

    private JsonNode createDefaultPositionNode() {
        ObjectNode positionNode = JsonNodeFactory.instance.objectNode();
        positionNode.put(LNG, 0.0);
        positionNode.put(LAT, 0.0);
        return positionNode;
    }
}
