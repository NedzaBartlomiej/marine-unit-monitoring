package pl.bartlomiej.marineunitmonitoring.user.dto;

import lombok.*;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReadDto {

    private String id;

    private String username;

    private String email;

    private List<TrackedShip> trackedShips;
}
