package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ship_track_history")
public class ShipTrack {

    @JsonIgnore
    private ObjectId id;
    private Long mmsi;
    private Double x;
    private Double y;
    private LocalDateTime reading_time;

    ShipTrack(Long mmsi, Double x, Double y) {
        this.mmsi = mmsi;
        this.x = x;
        this.y = y;
        this.reading_time = now();
    }
}
