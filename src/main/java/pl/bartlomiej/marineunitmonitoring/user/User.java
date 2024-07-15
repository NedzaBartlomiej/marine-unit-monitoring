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
    private List<String> openIds;
    private String username;
    private String email;
    private String password;
    private Boolean isVerified = false;
    private List<TrackedShip> trackedShips;
    private List<Role> roles;

    public User(String username, String email, List<Role> roles, List<String> openIds, Boolean isVerified) {
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.openIds = openIds;
        this.isVerified = isVerified;
    }
}