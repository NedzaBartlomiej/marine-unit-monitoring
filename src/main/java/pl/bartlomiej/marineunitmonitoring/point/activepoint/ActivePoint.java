package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "active_points")
public class ActivePoint {
    private String id;
    private Long mmsi; // todo replace mmsi to be an id value
    private String name;

    public ActivePoint(Long mmsi, String name) {
        this.mmsi = mmsi;
        this.name = name;
    }
}
