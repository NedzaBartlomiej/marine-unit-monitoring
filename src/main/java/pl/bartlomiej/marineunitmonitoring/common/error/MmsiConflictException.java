package pl.bartlomiej.marineunitmonitoring.common.error;

public class MmsiConflictException extends RuntimeException {
    public MmsiConflictException(String message) {
        super(message);
    }
}