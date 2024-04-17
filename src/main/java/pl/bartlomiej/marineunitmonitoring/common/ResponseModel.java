package pl.bartlomiej.marineunitmonitoring.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel<T> {

    private final HttpStatus httpStatus;
    private final Integer httpStatusCode;
    private final String message;
    @Builder.Default
    private final LocalDateTime readingTime = now(systemDefault());
    private final Map<String, T> body;
}