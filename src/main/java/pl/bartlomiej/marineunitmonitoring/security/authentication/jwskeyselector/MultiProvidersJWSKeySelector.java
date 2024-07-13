package pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector.config.JWSKeySelectorConfig;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector.config.properties.MultiProvidersJWSKeySelectorProperties;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector.config.properties.Provider;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl;

import java.net.URL;
import java.security.Key;
import java.util.List;

@Slf4j
@Component
public class MultiProvidersJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final JWSKeySelectorConfig jwsKeySelectorConfig;
    private final MultiProvidersJWSKeySelectorProperties keySelectorProperties;
    private final JWTService jwtService;

    public MultiProvidersJWSKeySelector(JWSKeySelectorConfig jwsKeySelectorConfig, MultiProvidersJWSKeySelectorProperties keySelectorProperties, JWTService jwtService) {
        this.jwsKeySelectorConfig = jwsKeySelectorConfig;
        this.keySelectorProperties = keySelectorProperties;
        this.jwtService = jwtService;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext) throws KeySourceException {
        if (jwtClaimsSet.getIssuer().equals(JWTServiceImpl.TOKEN_ISSUER)) {
            log.info("Returning secret key for registration authentication based token.");
            return List.of(jwtService.getSigningKey());
        }

        log.info("Attempting to return key set for OAuth2 issuer.");
        var selector = jwsKeySelectorConfig.getJWSKeySelector(
                this.getJwksUrl(jwtClaimsSet.getIssuer())
        );
        log.info("Returning key set.");
        return selector.selectJWSKeys(jwsHeader, securityContext);
    }

    private URL getJwksUrl(String issuer) {
        log.info("Recognising token provider and returning dependent JWKs url.");
        return keySelectorProperties.providers().stream()
                .filter(provider ->
                        provider.issuerUri().equals(issuer)
                ).map(Provider::jwksUri)
                .findFirst()
                .orElseThrow(); // todo handle it (jwks url for provided provider not found)
    }
}