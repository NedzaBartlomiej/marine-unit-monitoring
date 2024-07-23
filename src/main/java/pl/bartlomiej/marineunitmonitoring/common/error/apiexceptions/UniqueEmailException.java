package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class UniqueEmailException extends RuntimeException {

    public UniqueEmailException() {
        super("EMAIL_ALREADY_USED");
    }
}
