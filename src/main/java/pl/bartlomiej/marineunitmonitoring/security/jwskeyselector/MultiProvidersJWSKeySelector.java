package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;

@Component
public class MultiProvidersJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final Map<String, URL> providers;

    public MultiProvidersJWSKeySelector(Map<String, URL> providers) {
        this.providers = providers;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext) throws KeySourceException {
        URL jwksUri = providers.get(jwtClaimsSet.getIssuer());
        var selector = JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(jwksUri);
        return selector.selectJWSKeys(jwsHeader, securityContext);
    }
}