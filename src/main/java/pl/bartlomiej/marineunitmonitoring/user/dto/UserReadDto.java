package pl.bartlomiej.marineunitmonitoring.user.dto;

import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;

import java.util.List;

public class UserReadDto {

    private String id;

    private String username;

    private String email;

    private List<TrackedShip> trackedShips;

    public UserReadDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<TrackedShip> getTrackedShips() {
        return trackedShips;
    }

    public void setTrackedShips(List<TrackedShip> trackedShips) {
        this.trackedShips = trackedShips;
    }
}
