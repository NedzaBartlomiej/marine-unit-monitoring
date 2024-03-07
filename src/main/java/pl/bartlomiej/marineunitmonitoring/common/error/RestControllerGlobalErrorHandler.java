package pl.bartlomiej.marineunitmonitoring.common.error;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestControllerAdvice
public class RestControllerGlobalErrorHandler {

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Void> handleNoContentException(NoContentException e) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseModel<Void>> handleValidationException(BindingResult bindingResult, ServerRequest request) {
        return ResponseEntity.badRequest().body(
                ResponseModel.<Void>builder()
                        .httpStatus(BAD_REQUEST)
                        .httpStatusCode(BAD_REQUEST.value())
                        .message(requireNonNull(bindingResult.getFieldError()).getDefaultMessage())
                        .build()
        );
    }

    @ExceptionHandler(MmsiConflictException.class)
    public ResponseEntity<ResponseModel<Void>> handleMmsiConflictException(MmsiConflictException e) {
        return ResponseEntity.status(CONFLICT).body(
                ResponseModel.<Void>builder()
                        .httpStatus(CONFLICT)
                        .httpStatusCode(CONFLICT.value())
                        .message(e.getMessage())
                        .build()
        );
    }

}
