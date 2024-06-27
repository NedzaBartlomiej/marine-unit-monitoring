package pl.bartlomiej.marineunitmonitoring.common.error;

import lombok.Getter;

@Getter
public enum SecurityError {
    FORBIDDEN("You don't have the required permissions."),
    UNAUTHORIZED_CREDENTIALS("Bad authentication credentials."),
    UNAUTHORIZED_AUTHENTICATION("You need to authenticate to access this resource.");

    private final String message;

    SecurityError(String message) {
        this.message = message;
    }
}
