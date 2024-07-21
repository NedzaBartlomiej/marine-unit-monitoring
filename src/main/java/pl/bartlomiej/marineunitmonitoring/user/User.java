package pl.bartlomiej.marineunitmonitoring.user;

import org.springframework.data.mongodb.core.mapping.Document;
import pl.bartlomiej.marineunitmonitoring.user.nested.Role;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;

import java.util.List;

@Document(collection = "users")
public class User {

    private String id;
    private List<String> openIds;
    private String username;
    private String email;
    private String password;
    private Boolean isVerified = false;
    private Boolean isLocked = false;
    private List<TrackedShip> trackedShips;
    private List<Role> roles;
    private List<String> trustedIpAddresses;

    public User() {
    }

    public User(String username, String email, List<Role> roles, List<String> openIds, Boolean isVerified) {
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.openIds = openIds;
        this.isVerified = isVerified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getOpenIds() {
        return openIds;
    }

    public void setOpenIds(List<String> openIds) {
        this.openIds = openIds;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public List<TrackedShip> getTrackedShips() {
        return trackedShips;
    }

    public void setTrackedShips(List<TrackedShip> trackedShips) {
        this.trackedShips = trackedShips;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<String> getTrustedIpAddresses() {
        return trustedIpAddresses;
    }

    public void setTrustedIpAddresses(List<String> trustedIpAddresses) {
        this.trustedIpAddresses = trustedIpAddresses;
    }
}