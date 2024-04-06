package pl.bartlomiej.marineunitmonitoring.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.bartlomiej.marineunitmonitoring.user.nested.TrackedShip;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    private String id;

    private String username;

    private String email;

    private String password;

    private List<TrackedShip> trackedShips;
}