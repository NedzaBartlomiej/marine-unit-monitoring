package pl.bartlomiej.marineunitmonitoring.common.error;

public class MmsiConflictException extends RuntimeException {
    public MmsiConflictException(String message) {
        super(message);
    }

    //todo - fix zeby uzywalo message
    public enum Message {
        SHIP_IS_ALREADY_TRACKED("Ship is already tracked."),
        INVALID_SHIP("Invalid ship.");

        Message(String message) {
        }
    }
}