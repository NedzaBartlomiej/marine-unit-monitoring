package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config.properties;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
@ConfigurationPropertiesBinding
public class UrlConverter implements Converter<String, URL> {
    @Override
    public URL convert(@NonNull String source) {
        try {
            return new URI(source).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + source + ": " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + source + ": " + e.getMessage());
        }
    }
}
