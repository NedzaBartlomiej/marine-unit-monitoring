package pl.bartlomiej.marineunitmonitoring.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.bartlomiej.marineunitmonitoring.user.nested.Role;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;

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
    private List<Role> roles;

    public User(String id, String username, String email, List<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}