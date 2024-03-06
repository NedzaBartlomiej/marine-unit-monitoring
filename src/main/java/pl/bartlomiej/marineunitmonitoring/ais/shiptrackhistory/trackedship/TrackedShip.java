package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "tracked_ships")
@AllArgsConstructor
@NoArgsConstructor
public class TrackedShip {

    @JsonIgnore
    private ObjectId id;

    @NotNull(message = "Ship mmsi required.")
    private Long mmsi;
}