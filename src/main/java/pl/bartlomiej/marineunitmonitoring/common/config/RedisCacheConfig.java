package pl.bartlomiej.marineunitmonitoring.common.config;

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
import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class RedisCacheConfig {

    public static final String AIS_AUTH_TOKEN_CACHE_NAME = "AisAuthToken";
    public static final String ADDRESS_COORDS_CACHE_NAME = "AddressCoords";
    public static final String POINTS_CACHE_NAME = "Points";
    private final AisApiAccessTokenService accessTokenService;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(
                        AIS_AUTH_TOKEN_CACHE_NAME,
                        defaultCacheConfig().entryTtl(ofHours(1)))
                .withCacheConfiguration(
                        ADDRESS_COORDS_CACHE_NAME,
                        defaultCacheConfig()
                )
                .withCacheConfiguration(
                        POINTS_CACHE_NAME,
                        defaultCacheConfig().entryTtl(ofMinutes(30))
                );
    }

    @EventListener(ApplicationReadyEvent.class)
    @CachePut(cacheNames = "AisAuthToken", key = "#result")
    public Mono<String> refreshToken() {
        log.info("Refreshing ais api auth token in cache.");
        return accessTokenService.getAisAuthTokenWithoutCache().cache();
    }
}
