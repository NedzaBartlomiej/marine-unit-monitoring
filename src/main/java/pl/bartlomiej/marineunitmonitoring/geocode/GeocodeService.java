package pl.bartlomiej.marineunitmonitoring.geocode;

import pl.bartlomiej.marineunitmonitoring.geocode.Position;
import reactor.core.publisher.Flux;

public interface GeocodeService {
    Flux<Position> getAddressCoordinates(String address);
}
