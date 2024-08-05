package pl.bartlomiej.marineunitmonitoring.security.exceptionhandling;

public enum SecurityError {
    LOCKED("ACCOUNT_LOCKED"),
    FORBIDDEN("INSUFFICIENT_PERMISSIONS"),
    UNAUTHORIZED_CREDENTIALS("BAD_CREDENTIALS"),
    UNAUTHORIZED_AUTHENTICATION("UNAUTHENTICATED"),
    INVALID_TOKEN("INVALID_TOKEN"),
    INTERNAL_ERROR("SERVER_ERROR");

    private final String message;

    SecurityError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}