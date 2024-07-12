package pl.bartlomiej.marineunitmonitoring.common.error;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException() {
        super("Invalid token.");
    }
}
