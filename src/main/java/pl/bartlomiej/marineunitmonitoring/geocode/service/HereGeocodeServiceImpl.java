package pl.bartlomiej.marineunitmonitoring.geocode.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.bartlomiej.marineunitmonitoring.geocode.Position;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class HereGeocodeServiceImpl implements GeocodeService {
    private static final String GEOCODE_API_KEY = "efxdpJLDw0gSytalN90kAh8L63kk6X8Yrmb2Gfa6Q2o";
    private final WebClient webClient;

    //    @Cacheable(cacheNames = "AddressCoords")
    public Flux<Position> getAddressCoords(String address) {
        return this.getGeocodeFromApi(address)
                .map(response -> this.getPositionFromResponse(response, address));
    }

    @NonNull
    private Flux<JsonNode> getGeocodeFromApi(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.error("Null address, skipping request sending.");
            return Flux.empty();
        }
        log.info("Requesting geocode for address: {}", address);
        return webClient
                .get()
                .uri(this.getGeocodeApiUrl(address))
                .retrieve()
                .bodyToFlux(JsonNode.class);
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
