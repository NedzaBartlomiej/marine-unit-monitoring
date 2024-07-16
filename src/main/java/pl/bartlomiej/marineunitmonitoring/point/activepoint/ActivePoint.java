package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "active_points")
public class ActivePoint {

    @Id
    private String mmsi;
    private String name;

    public ActivePoint() {
    }

    public ActivePoint(String mmsi, String name) {
        this.mmsi = mmsi;
        this.name = name;
    }

    public String getMmsi() {
        return mmsi;
    }

    public String getName() {
        return name;
    }
}