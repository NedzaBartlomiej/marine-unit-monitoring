package pl.bartlomiej.marineunitmonitoring.common.error;

public class UniqueEmailException extends RuntimeException {

    public static final String MESSAGE = "User with this email already exists.";

    public UniqueEmailException() {
        super(MESSAGE);
    }
}
