package pl.bartlomiej.marineunitmonitoring.common.error;

public class UniqueEmailException extends RuntimeException {
    public UniqueEmailException() {
        super("User with this email already exists.");
    }
}
