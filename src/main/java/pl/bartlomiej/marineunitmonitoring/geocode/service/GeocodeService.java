package pl.bartlomiej.marineunitmonitoring.geocode.service;

import pl.bartlomiej.marineunitmonitoring.geocode.Position;

public interface GeocodeService {
    Position getAddressCoords(String address);
}
