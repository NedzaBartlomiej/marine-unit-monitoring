package pl.bartlomiej.marineunitmonitoring.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserSaveDto {

    @NotBlank(message = "EMPTY_USERNAME")
    private String username;

    @NotBlank(message = "EMPTY_EMAIL")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "EMPTY_PASSWORD")
    private String password;

    public UserSaveDto() {
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
}
