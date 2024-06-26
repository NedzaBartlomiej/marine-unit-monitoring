package pl.bartlomiej.marineunitmonitoring.common.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel<T> {

    private final HttpStatus httpStatus;
    private final Integer httpStatusCode;
    private final String message;
    @Builder.Default
    private final LocalDateTime readingTime = now(); // todo - LocalDateTime type not working in reactive spring security response
    private final Map<String, T> body;
}