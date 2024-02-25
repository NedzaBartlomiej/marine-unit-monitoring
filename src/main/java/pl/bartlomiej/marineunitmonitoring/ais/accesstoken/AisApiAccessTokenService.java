package pl.bartlomiej.marineunitmonitoring.ais.accesstoken;

import reactor.core.publisher.Mono;

public interface AisApiAccessTokenService {
    Mono<String> getRefreshedToken();
}
