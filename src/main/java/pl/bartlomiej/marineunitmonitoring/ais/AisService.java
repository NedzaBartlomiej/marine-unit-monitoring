package pl.bartlomiej.marineunitmonitoring.ais;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AisService {
    Flux<AisShip> fetchLatestShips();

    Mono<List<JsonNode>> fetchShipsByIdentifiers(List<Long> identifiers);
}
