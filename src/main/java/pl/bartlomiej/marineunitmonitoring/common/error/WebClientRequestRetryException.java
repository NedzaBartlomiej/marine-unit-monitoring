package pl.bartlomiej.marineunitmonitoring.common.error;

public class WebClientRequestRetryException extends RuntimeException {
    public WebClientRequestRetryException(String message) {
        super(message);
    }
}
