package pl.bartlomiej.marineunitmonitoring.user.nested.trackedship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackedShip {

    private String mmsi;

    private String name;
}