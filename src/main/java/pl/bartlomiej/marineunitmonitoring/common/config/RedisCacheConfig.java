package pl.bartlomiej.marineunitmonitoring.common.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofMillis;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
public class RedisCacheConfig {

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
}
