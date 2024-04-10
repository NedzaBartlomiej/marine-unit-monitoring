package pl.bartlomiej.marineunitmonitoring.shiptracking.trackedship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackedShip {

    private Long mmsi;

    private String name;
}