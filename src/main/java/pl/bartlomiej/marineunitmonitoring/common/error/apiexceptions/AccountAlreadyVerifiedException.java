package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class AccountAlreadyVerifiedException extends RuntimeException {
    public AccountAlreadyVerifiedException() {
        super("ALREADY_VERIFIED");
    }
}
