package pl.bartlomiej.marineunitmonitoring.ais;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AisService {
    Flux<AisShip> fetchLatestShips();

    Flux<JsonNode> fetchShipsByIdentifiers(List<Long> identifiers);
}
