package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class WebClientRequestRetryException extends RuntimeException {
    public WebClientRequestRetryException(String message) {
        super(message);
    }
}