package pl.bartlomiej.marineunitmonitoring.point.activepoint.service.sync;

public interface ActivePointsSyncService {

    String getName(Long mmsi);

    Boolean isPointActive(Long mmsi);
}
