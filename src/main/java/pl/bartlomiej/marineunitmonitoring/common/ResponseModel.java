package pl.bartlomiej.marineunitmonitoring.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel<T> {

    private final HttpStatus httpStatus;
    private final Integer httpStatusCode;
    private final String message;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final T body;
}