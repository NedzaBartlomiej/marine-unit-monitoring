package pl.bartlomiej.marineunitmonitoring.shiptracking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ship_track_history")
public class ShipTrack {

    @JsonIgnore
    private String id;
    private Long mmsi;
    private Double x;
    private Double y;
    private LocalDateTime readingTime = now(systemDefault());

    ShipTrack(Long mmsi, Double x, Double y) {
        this.mmsi = mmsi;
        this.x = x;
        this.y = y;
    }
}