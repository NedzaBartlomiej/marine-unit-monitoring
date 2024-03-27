package pl.bartlomiej.marineunitmonitoring.common.error;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Not found.");
    }
}
