package pl.bartlomiej.marineunitmonitoring.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;

import java.nio.file.AccessDeniedException;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class RestControllerGlobalErrorHandler {

    private ResponseEntity<ResponseModel<Void>> buildErrorResponse(String message, HttpStatus httpStatus) {
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseModel<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(SecurityError.FORBIDDEN.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseModel<Void>> handleBadCredentialsException(BadCredentialsException e) {
        return buildErrorResponse(SecurityError.UNAUTHORIZED_CREDENTIALS.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseModel<Void>> handleAuthenticationException(AuthenticationException e) {
        return buildErrorResponse(SecurityError.UNAUTHORIZED_AUTHENTICATION.getMessage(), UNAUTHORIZED);
    }

}
