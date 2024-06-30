package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JWSKeySelectorConfig {

    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public JWSKeySelectorConfig(DefaultJWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Bean
    DefaultJWTProcessor<SecurityContext> jwtProcessor() {
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(this.multiProvidersJWSKeySelector());
        return jwtProcessor;
    }

    @Bean
    Map<String, JWKSource<SecurityContext>> jwkSources() {
        Map<String, JWKSource<SecurityContext>> jwkSources = new HashMap<>();
        Arrays.stream(Provider.values())
                .forEach(provider ->
                        jwkSources.put(provider.issuerUri, provider.jwkSetUri)
                );
        return jwkSources;
    }

    @Bean
    MultiProvidersJWSKeySelector multiProvidersJWSKeySelector() {
        return new MultiProvidersJWSKeySelector(this.jwkSources());
    }

    // todo - read about JWKSource<> implementations
    private enum Provider {

        GOOGLE(),
        FACEBOOK();

        private final String issuerUri;
        private final JWKSource<SecurityContext> jwkSetUri;

        Provider(String issuerUri, JWKSource<SecurityContext> jwkSetUri) {
            this.issuerUri = issuerUri;
            this.jwkSetUri = jwkSetUri;
        }
    }
}