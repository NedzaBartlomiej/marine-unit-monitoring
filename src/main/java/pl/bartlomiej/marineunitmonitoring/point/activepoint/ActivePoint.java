package pl.bartlomiej.marineunitmonitoring.point.activepoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "active_points")
public class ActivePoint {
    @Id
    private String mmsi;
    private String name;
}