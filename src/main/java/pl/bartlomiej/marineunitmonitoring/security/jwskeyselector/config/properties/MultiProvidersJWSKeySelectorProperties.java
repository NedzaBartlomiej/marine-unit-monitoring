package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(
        prefix = "project-properties.oauth2.resource-server.multi-providers-jws-key-selector"
)
public record MultiProvidersJWSKeySelectorProperties(List<Provider> providers) {
}
