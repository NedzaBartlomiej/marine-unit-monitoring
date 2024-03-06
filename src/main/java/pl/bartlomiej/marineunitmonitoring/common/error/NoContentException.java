package pl.bartlomiej.marineunitmonitoring.common.error;

public class NoContentException extends RuntimeException {
    public NoContentException() {
        super("No content found.");
    }
}
