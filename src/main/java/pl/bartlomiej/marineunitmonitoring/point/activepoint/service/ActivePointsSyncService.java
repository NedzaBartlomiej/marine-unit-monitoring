package pl.bartlomiej.marineunitmonitoring.point.activepoint.service;

public interface ActivePointsSyncService {

    String getName(Long mmsi);

    Boolean isPointActive(Long mmsi);
}
