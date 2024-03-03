package pl.bartlomiej.marineunitmonitoring.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import pl.bartlomiej.marineunitmonitoring.ais.accesstoken.AisApiAccessTokenService;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofHours;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class RedisCacheConfig {

    private final AisApiAccessTokenService accessTokenService;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(
                        "AisAuthToken",
                        defaultCacheConfig().entryTtl(ofHours(1)))
                .withCacheConfiguration(
                        "AddressCoords",
                        defaultCacheConfig()
                );
    }

    @EventListener(ApplicationReadyEvent.class)
    @CachePut(cacheNames = "AisAuthToken", key = "#result")
    public Mono<String> refreshToken() {
        log.info("Refreshing ais api auth token in cache.");
        return accessTokenService.getAisAuthTokenWithoutCache().cache();
    }
}
