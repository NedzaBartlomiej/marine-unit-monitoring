package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class UnverifiedAccountException extends AuthenticationException {

    public static final String MESSAGE = "UNVERIFIED_ACCOUNT";

    public UnverifiedAccountException() {
        super(MESSAGE);
    }
}
