package pl.bartlomiej.marineunitmonitoring.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;


@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient
                .builder()
                .filter(this.buildRetryExchangeFilterFunction())
                .build();
    }


    // todo ogarnac ale trudne to to nie jest
    private ExchangeFilterFunction buildRetryExchangeFilterFunction() {
        return (request, next) -> next.exchange(request)
                .flatMap(clientResponse -> Mono.just(clientResponse)
                        .filter(response -> clientResponse.statusCode().isError())
                        .flatMap(response -> clientResponse.createException())
                        .flatMap(Mono::error)
                        .thenReturn(clientResponse))
                .retryWhen(this.retryWhenTooManyRequests());
    }

    private RetryBackoffSpec retryWhenTooManyRequests() {
        return Retry.backoff(4L, Duration.ofMillis(250L))
                .filter(this::isTooManyRequestsException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    }

    private boolean isTooManyRequestsException(final Throwable throwable) {
        return throwable instanceof WebClientResponseException.TooManyRequests;
    }
    
}
