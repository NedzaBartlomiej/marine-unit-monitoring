package pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector.config.properties;

import java.net.URL;

public record Provider(String issuerUri, URL jwksUri) {
}
