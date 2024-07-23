package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class RegisterBasedUserNotFoundException extends AuthenticationException {
    public RegisterBasedUserNotFoundException() {
        super("REGISTRATION_BASED_USER_NOT_FOUND");
    }
}
