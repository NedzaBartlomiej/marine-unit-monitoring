package pl.bartlomiej.marineunitmonitoring.geocoding.service;

import pl.bartlomiej.marineunitmonitoring.geocoding.Position;
import reactor.core.publisher.Flux;

public interface GeocodeService {
    Flux<Position> getAddressCoordinates(String address);
}
