package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;

import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MultiProvidersJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final Map<String, JWKSource<SecurityContext>> jwkSources;

    public MultiProvidersJWSKeySelector(Map<String, JWKSource<SecurityContext>> jwkSources) {
        this.jwkSources = jwkSources;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext) throws KeySourceException {
        String issuer = jwtClaimsSet.getIssuer();
        JWKSource<SecurityContext> jwkSource = jwkSources.get(issuer);

        if (jwkSource == null) {
            return Collections.emptyList();
        }

        JWKSelector jwkSelector = new JWKSelector(JWKMatcher.forJWSHeader(jwsHeader));

        try {
            return jwkSource.get(jwkSelector, securityContext)
                    .stream()
                    .map(this::convertJWKToKey)
                    .toList();
        } catch (Exception e) {
            throw new KeySourceException("Error retrieving keys from JWKSource", e);
        }
    }

    private Key convertJWKToKey(JWK jwk) {
        try {
            if (jwk instanceof RSAKey) {
                return ((RSAKey) jwk).toPublicKey();
            } else if (jwk instanceof ECKey) {
                return ((ECKey) jwk).toPublicKey();
            } else if (jwk instanceof OctetSequenceKey) {
                return ((OctetSequenceKey) jwk).toSecretKey();
            }
            throw new IllegalArgumentException("Unsupported JWK type: " + jwk.getClass().getSimpleName());
        } catch (JOSEException e) {
            throw new RuntimeException("Error converting JWK to Key", e);
        }
    }
}