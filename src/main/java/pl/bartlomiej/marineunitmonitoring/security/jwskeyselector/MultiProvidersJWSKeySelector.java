package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config.JWSKeySelectorConfig;
import pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config.properties.MultiProvidersJWSKeySelectorProperties;
import pl.bartlomiej.marineunitmonitoring.security.jwskeyselector.config.properties.Provider;

import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class MultiProvidersJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final JWSKeySelectorConfig jwsKeySelectorConfig;
    private final MultiProvidersJWSKeySelectorProperties keySelectorProperties;

    public MultiProvidersJWSKeySelector(JWSKeySelectorConfig jwsKeySelectorConfig, MultiProvidersJWSKeySelectorProperties keySelectorProperties) {
        this.jwsKeySelectorConfig = jwsKeySelectorConfig;
        this.keySelectorProperties = keySelectorProperties;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext) throws KeySourceException {
        var selector = jwsKeySelectorConfig.getJWSKeySelector(
                this.getJwksUrl(jwtClaimsSet.getIssuer())
        );
        return selector.selectJWSKeys(jwsHeader, securityContext);
    }

    private URL getJwksUrl(String issuer) {
        return keySelectorProperties.providers().stream()
                .filter(provider ->
                        provider.issuerUri().equals(issuer)
                ).map(Provider::jwksUri)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No JWKS found for issuer " + issuer));
    }
}