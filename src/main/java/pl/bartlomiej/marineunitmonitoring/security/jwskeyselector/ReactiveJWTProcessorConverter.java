package pl.bartlomiej.marineunitmonitoring.security.jwskeyselector;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.core.convert.converter.Converter;
import reactor.core.publisher.Mono;

public class ReactiveJWTProcessorConverter implements Converter<JWT, Mono<JWTClaimsSet>> {

    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    public ReactiveJWTProcessorConverter(DefaultJWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    public Mono<JWTClaimsSet> convert(JWT source) {
        try {
            return Mono.just(jwtProcessor.process(source, null));
        } catch (BadJOSEException | JOSEException e) {
            return Mono.error(e);
        }
    }
}
