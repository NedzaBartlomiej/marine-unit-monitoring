package pl.bartlomiej.marineunitmonitoring.security.exceptionhandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseModelServerAccessDeniedHandler extends ResponseModelServerExceptionHandler implements ServerAccessDeniedHandler {
    protected ResponseModelServerAccessDeniedHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return super.processException(denied, exchange);
    }
}