package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.ReactiveJWTProcessorConverter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JWSKeySelectorConfig {

    private final Map<String, JWSAlgorithmFamilyJWSKeySelector<SecurityContext>> jwsKeySelectorMap = new HashMap<>();

    @Bean
    ReactiveJwtDecoder jwtDecoder(ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {
        var reactiveJWTProcessor = new ReactiveJWTProcessorConverter((DefaultJWTProcessor<SecurityContext>) jwtProcessor);
        return new NimbusReactiveJwtDecoder(reactiveJWTProcessor);
    }

    @Bean
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor(
            JWTClaimsSetAwareJWSKeySelector<SecurityContext> jwsKeySelector
    ) {
        var jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(jwsKeySelector);
        return jwtProcessor;
    }


    public JWSAlgorithmFamilyJWSKeySelector<SecurityContext> getJWSKeySelector(URL jwksUrl) {
        String urlString = jwksUrl.toString();
        if (!jwsKeySelectorMap.containsKey(urlString)) {
            jwsKeySelectorMap.put(urlString, this.createJWSKeySelector(jwksUrl));
        }
        return jwsKeySelectorMap.get(urlString);
    }

    private JWSAlgorithmFamilyJWSKeySelector<SecurityContext> createJWSKeySelector(URL jwksUrl) {
        try {
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(jwksUrl);
        } catch (KeySourceException e) {
            throw new RuntimeException(e);
        }
    }
}