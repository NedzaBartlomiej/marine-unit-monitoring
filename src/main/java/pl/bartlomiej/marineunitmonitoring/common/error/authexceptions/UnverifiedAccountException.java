package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class UnverifiedAccountException extends AuthenticationException {

    public static final String MESSAGE = "Unverified user account. Verify email and try again.";

    public UnverifiedAccountException() {
        super(MESSAGE);
    }
}
