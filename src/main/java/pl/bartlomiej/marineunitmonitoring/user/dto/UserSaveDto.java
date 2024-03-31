package pl.bartlomiej.marineunitmonitoring.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveDto {

    @NotBlank(message = "Username required.")
    private String username;

    @NotBlank(message = "Email required.")
    @Email
    private String email;

    @NotBlank(message = "Password required.")
    private String password;
}
