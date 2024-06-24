package pl.bartlomiej.marineunitmonitoring.point.activepoint.service.sync;

// todo - replace with reactive solution
public interface ActivePointsSyncService {

    String getName(Long mmsi);

    Boolean isPointActive(Long mmsi);
}
