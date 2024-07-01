package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JWSKeySelectorConfig {

    @Bean
    ReactiveJwtDecoder jwtDecoder(ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {
        var converter = new ReactiveJWTProcessorConverter((DefaultJWTProcessor<SecurityContext>) jwtProcessor);
        return new NimbusReactiveJwtDecoder(converter);
    }

    @Bean
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor(
            JWTClaimsSetAwareJWSKeySelector<SecurityContext> jwsKeySelector
    ) {
        var jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(jwsKeySelector);
        return jwtProcessor;
    }

    @Bean
    MultiProvidersJWSKeySelector multiProvidersJWSKeySelector(Map<String, URL> providers) {
        return new MultiProvidersJWSKeySelector(providers);
    }

    @Bean
    Map<String, URL> providers() {
        Map<String, URL> providers = new HashMap<>();
        Arrays.stream(Provider.values())
                .forEach(provider ->
                        providers.put(provider.issuerUri, provider.jwkSetUri)
                );
        return providers;
    }


    public enum Provider {

        GOOGLE(),
        FACEBOOK();

        private final String issuerUri;
        private final URL jwkSetUri;

        Provider(String issuerUri, URL jwkSetUri) {
            this.issuerUri = issuerUri;
            this.jwkSetUri = jwkSetUri;
        }
    }
}