package pl.bartlomiej.marineunitmonitoring.common.cache;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CacheNameProvider {

    @Value("${vars.cache.address-coords.name}")
    private String addressCoordsName;

    @Value("${vars.cache.ais-auth-token.name}")
    private String aisAuthTokenName;
}
