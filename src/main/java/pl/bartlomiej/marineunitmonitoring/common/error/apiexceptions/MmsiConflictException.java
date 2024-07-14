package pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions;

public class MmsiConflictException extends RuntimeException {
    public MmsiConflictException(String message) {
        super(message);
    }

    public enum Message {
        SHIP_IS_ALREADY_TRACKED("Ship is already tracked."),
        INVALID_SHIP("Invalid ship.");

        public final String message;

        Message(String message) {
            this.message = message;
        }
    }
}