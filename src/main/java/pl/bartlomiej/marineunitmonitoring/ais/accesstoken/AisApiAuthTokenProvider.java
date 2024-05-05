package pl.bartlomiej.marineunitmonitoring.ais.accesstoken;

import reactor.core.publisher.Mono;

public interface AisApiAuthTokenProvider {
    Mono<String> getAisAuthToken();

    Mono<String> getAisAuthTokenWithoutCache();
}
