package pl.bartlomiej.marineunitmonitoring.common.error.authexceptions;

import org.springframework.security.core.AuthenticationException;

public class JWKsUrlNotFoundException extends AuthenticationException {
    public JWKsUrlNotFoundException() {
        super("JWK_SET_URL_NOT_FOUND");
    }
}
