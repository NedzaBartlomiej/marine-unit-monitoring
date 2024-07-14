package pl.bartlomiej.marineunitmonitoring.security.exceptionhandling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.common.error.authexceptions.InvalidTokenException;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatusCode.valueOf;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public abstract class ResponseModelServerExceptionHandler {

    private final ObjectMapper objectMapper;

    protected ResponseModelServerExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected Mono<Void> processException(final Exception exception, ServerWebExchange exchange) {
        switch (exception) {
            case AccessDeniedException ignoredAccessDeniedException -> {
                return writeExchange(exchange,
                        buildErrorResponse(FORBIDDEN, SecurityError.FORBIDDEN.getMessage()));
            }
            case BadCredentialsException ignoredBadCredentialsException -> {
                return writeExchange(exchange,
                        buildErrorResponse(UNAUTHORIZED, SecurityError.UNAUTHORIZED_CREDENTIALS.getMessage()));
            }
            case InvalidTokenException invalidTokenException -> {
                return writeExchange(exchange,
                        buildErrorResponse(UNAUTHORIZED, invalidTokenException.getMessage()));
            }
            case AuthenticationException ignoredAuthenticationException -> {
                return writeExchange(exchange,
                        buildErrorResponse(UNAUTHORIZED, SecurityError.UNAUTHORIZED_AUTHENTICATION.getMessage()));
            }
            default -> {
                log.error("Unhandled error message: {}, Exception {}", exception.getMessage(), exception.getClass().getName());
                log.error("Cause: {}, Stack Trace: {}", exception.getCause(), exception.getStackTrace());
                return writeExchange(exchange,
                        buildErrorResponse(INTERNAL_SERVER_ERROR, SecurityError.INTERNAL_ERROR.getMessage()));
            }
        }
    }

    protected ResponseModel<Void> buildErrorResponse(HttpStatus httpStatus, String message) {
        return ResponseModel.<Void>builder()
                .httpStatus(httpStatus)
                .httpStatusCode(httpStatus.value())
                .message(message)
                .build();
    }

    protected Mono<Void> writeExchange(final ServerWebExchange exchange, final ResponseModel<Void> responseModel) {
        exchange.getResponse().setStatusCode(valueOf(responseModel.getHttpStatusCode()));
        exchange.getResponse().setRawStatusCode(responseModel.getHttpStatusCode());
        exchange.getResponse().getHeaders().setContentType(APPLICATION_JSON);

        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(responseModel);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        byte[] bytes = jsonResponse.getBytes(UTF_8);
        Flux<DataBuffer> bufferFlux = Flux.just(
                exchange.getResponse().bufferFactory().wrap(bytes)
        );

        return exchange.getResponse()
                .writeAndFlushWith(Mono.just(bufferFlux));
    }
}