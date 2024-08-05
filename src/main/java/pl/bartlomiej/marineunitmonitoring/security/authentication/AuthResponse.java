package pl.bartlomiej.marineunitmonitoring.security.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(String message, Map<String, String> tokens) {
}
