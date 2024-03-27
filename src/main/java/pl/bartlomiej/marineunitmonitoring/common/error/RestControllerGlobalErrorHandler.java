package pl.bartlomiej.marineunitmonitoring.common.error;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class RestControllerGlobalErrorHandler {

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Void> handleNoContentException(NoContentException e) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseModel<Void>> handleValidationException(BindingResult bindingResult) {
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

    @ExceptionHandler(WebClientRequestRetryException.class)
    public ResponseEntity<ResponseModel<Void>> handleWebClientRequestRetryException(WebClientRequestRetryException e) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                ResponseModel.<Void>builder()
                        .httpStatus(INTERNAL_SERVER_ERROR)
                        .httpStatusCode(INTERNAL_SERVER_ERROR.value())
                        .message(e.getMessage())
                        .build()
        );
    }

}
