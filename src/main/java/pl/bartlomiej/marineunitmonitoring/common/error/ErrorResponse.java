package pl.bartlomiej.marineunitmonitoring.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private HttpStatus httpStatus;
    private Integer httpStatusCode;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(HttpStatus httpStatus, Integer httpStatusCode, String message) {
        this.httpStatus = httpStatus;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}