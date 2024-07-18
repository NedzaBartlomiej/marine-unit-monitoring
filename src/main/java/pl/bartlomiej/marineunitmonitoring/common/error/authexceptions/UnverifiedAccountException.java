package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class UnverifiedAccountException extends AuthenticationException {
    public UnverifiedAccountException() {
        super("Unverified user account. Verify email and try again.");
    }
}
