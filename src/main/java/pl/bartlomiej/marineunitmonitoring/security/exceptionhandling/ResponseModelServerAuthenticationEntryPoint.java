package pl.bartlomiej.marineunitmonitoring.security.exceptionhandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseModelServerAuthenticationEntryPoint extends ResponseModelServerExceptionHandler implements ServerAuthenticationEntryPoint {

    public ResponseModelServerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return super.processException(ex, exchange);
    }
}