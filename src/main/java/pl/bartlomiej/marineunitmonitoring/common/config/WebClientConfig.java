package pl.bartlomiej.marineunitmonitoring.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.bartlomiej.marineunitmonitoring.common.error.WebClientRequestRetryException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;


@Configuration
@Slf4j
public class WebClientConfig {

    public static final long RETRY_REQUEST_DELAY = 250L;
    public static final long MAX_ATTEMPTS = 4L;

    @Bean
    public WebClient webClient() {

        return WebClient
                .builder()
                .filter(this.buildRetryExchangeFilterFunction())
                .build();
    }


    private ExchangeFilterFunction buildRetryExchangeFilterFunction() {
        return (request, next) -> next.exchange(request)
                .flatMap(clientResponse -> Mono.just(clientResponse)
                        .filter(response -> clientResponse.statusCode().isError())
                        .flatMap(response -> clientResponse.createException())
                        .flatMap(Mono::error)
                        .thenReturn(clientResponse))
                .retryWhen(this.retryWhenTooManyRequests())
                .doOnError(throwable -> {
                    log.error("Something go wrong on retrying request: {}", throwable.getMessage());
                    throw new WebClientRequestRetryException(throwable.getMessage());
                });
    }

    private RetryBackoffSpec retryWhenTooManyRequests() {
        return Retry.backoff(MAX_ATTEMPTS, Duration.ofMillis(RETRY_REQUEST_DELAY))
                .filter(this::isTooManyRequestsException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    }

    private boolean isTooManyRequestsException(final Throwable throwable) {
        boolean isTooManyRequestsEx = throwable instanceof WebClientResponseException.TooManyRequests;
        if (isTooManyRequestsEx) log.error("TooManyRequestsException -> retrying request.");
        return isTooManyRequestsEx;
    }

}
