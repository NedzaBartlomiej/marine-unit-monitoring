package pl.bartlomiej.marineunitmonitoring.common.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;

import static java.util.Map.of;

public class ControllerResponseUtil {
    public static <T> ResponseEntity<ResponseModel<T>> buildResponse(HttpStatus httpStatus, ResponseModel<T> responseModel) {
        return ResponseEntity.status(httpStatus).body(responseModel);
    }

    public static <T> ResponseModel<T> buildResponseModel(
            String message, HttpStatus httpStatus, T bodyValue, String bodyKey) {

        ResponseModel.ResponseModelBuilder<T> builder =
                ResponseModel.<T>builder()
                        .httpStatus(httpStatus)
                        .httpStatusCode(httpStatus.value());

        if (message != null) {
            builder.message(message);
        }

        if (bodyValue != null && bodyKey != null) {
            builder.body(of(bodyKey, bodyValue));
        }

        return builder.build();
    }
}
