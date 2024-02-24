package pl.bartlomiej.marineunitmonitoring.ais.service;

import pl.bartlomiej.marineunitmonitoring.map.Point;

import java.util.List;

public interface AisService {
    List<Point> getLatestAisPoints();
}
