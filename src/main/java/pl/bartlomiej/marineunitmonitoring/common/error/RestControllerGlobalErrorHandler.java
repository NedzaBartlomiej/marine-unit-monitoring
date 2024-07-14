package pl.bartlomiej.marineunitmonitoring.common.error;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.SecurityError;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class RestControllerGlobalErrorHandler {

    private static ResponseEntity<ResponseModel<Void>> buildErrorResponse(String message, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(
                ResponseModel.<Void>builder()
                        .httpStatus(httpStatus)
                        .httpStatusCode(httpStatus.value())
                        .message(message)
                        .build()
        );
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Void> handleNoContentException(NoContentException e) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UniqueEmailException.class)
    public ResponseEntity<ResponseModel<Void>> handleUniqueEmailException(UniqueEmailException e) {
        return buildErrorResponse(e.getMessage(), CONFLICT);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseModel<Void>> handleValidationException(BindingResult bindingResult) {
        return buildErrorResponse(requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(MmsiConflictException.class)
    public ResponseEntity<ResponseModel<Void>> handleMmsiConflictException(MmsiConflictException e) {
        return buildErrorResponse(e.getMessage(), CONFLICT);
    }

    @ExceptionHandler(WebClientRequestRetryException.class)
    public ResponseEntity<ResponseModel<Void>> handleWebClientRequestRetryException(WebClientRequestRetryException e) {
        return buildErrorResponse(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseModel<Void>> handleJwtException(JwtException e) {
        log.error("Invalid JWT: {}", e.getMessage());
        return buildErrorResponse(SecurityError.INVALID_TOKEN.getMessage(), UNAUTHORIZED);
    }
}
