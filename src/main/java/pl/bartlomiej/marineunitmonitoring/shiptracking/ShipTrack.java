package pl.bartlomiej.marineunitmonitoring.shiptracking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ship_track_history")
public class ShipTrack {

    @Id
    private String mmsi;
    private Double x;
    private Double y;
    private LocalDateTime readingTime = now();

    ShipTrack(String mmsi, Double x, Double y) {
        this.mmsi = mmsi;
        this.x = x;
        this.y = y;
    }
}