package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class UnverifiedUserException extends AuthenticationException {
    public UnverifiedUserException() {
        super("Unverified user account. Verify email and try again.");
    }
}
