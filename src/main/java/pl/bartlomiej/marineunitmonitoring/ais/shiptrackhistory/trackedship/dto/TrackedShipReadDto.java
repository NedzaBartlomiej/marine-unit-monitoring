package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackedShipReadDto {
    private Long mmsi;

    private String name;
}
