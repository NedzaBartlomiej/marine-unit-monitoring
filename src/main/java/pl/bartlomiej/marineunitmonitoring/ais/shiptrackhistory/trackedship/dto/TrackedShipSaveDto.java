package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackedShipSaveDto {
    @NotNull(message = "Ship mmsi required.")
    private Long mmsi;

    @JsonIgnore
    private String name;
}
