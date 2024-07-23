package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("NOT_FOUND");
    }
}
