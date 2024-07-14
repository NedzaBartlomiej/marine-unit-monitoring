package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class JWKsUrlNotFoundException extends AuthenticationException {
    public JWKsUrlNotFoundException() {
        super("JWKs url not found for a particular provider.");
    }
}
