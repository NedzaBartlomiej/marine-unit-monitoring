package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class NoContentException extends RuntimeException {
    public NoContentException() {
        super("No content found.");
    }
}
