package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class RegisterBasedUserNotFoundException extends AuthenticationException {
    public RegisterBasedUserNotFoundException() {
        super("Registration based user not found.");
    }
}
