package pl.bartlomiej.marineunitmonitoring.common.error;

import org.springframework.security.core.AuthenticationException;

public class JWKsUrlNotFoundException extends AuthenticationException {
    public JWKsUrlNotFoundException() {
        super("JWKs url not found for a particular provider.");
    }
}
