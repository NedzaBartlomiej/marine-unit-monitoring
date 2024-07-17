package pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.BadJwtException;
import pl.bartlomiej.marineunitmonitoring.common.error.authexceptions.JWKsUrlNotFoundException;
import reactor.core.publisher.Mono;

public class ReactiveJWTProcessorConverter implements Converter<JWT, Mono<JWTClaimsSet>> {

    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public ReactiveJWTProcessorConverter(DefaultJWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    // todo - test
    @Override
    public Mono<JWTClaimsSet> convert(@NonNull JWT source) {
        return Mono.fromCallable(() -> jwtProcessor.process(source, null))
                .onErrorMap(e -> {
                    if (e instanceof BadJOSEException || e instanceof JOSEException || e instanceof JWKsUrlNotFoundException) {
                        return new BadJwtException(e.getMessage(), e);
                    } else {
                        return e;
                    }
                });
    }
}
