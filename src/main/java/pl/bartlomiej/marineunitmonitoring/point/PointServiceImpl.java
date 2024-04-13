package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import pl.bartlomiej.marineunitmonitoring.ais.AisShip;
import pl.bartlomiej.marineunitmonitoring.common.error.NoContentException;
import pl.bartlomiej.marineunitmonitoring.geocode.GeocodeService;
import pl.bartlomiej.marineunitmonitoring.geocode.Position;
import reactor.core.publisher.Flux;

import static pl.bartlomiej.marineunitmonitoring.ais.nested.Geometry.X_COORDINATE_INDEX;
import static pl.bartlomiej.marineunitmonitoring.ais.nested.Geometry.Y_COORDINATE_INDEX;
import static pl.bartlomiej.marineunitmonitoring.common.config.RedisCacheConfig.POINTS_CACHE_NAME;
import static reactor.core.publisher.Flux.error;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    public static final String UNKNOWN_NOT_REPORTED = "UNKNOWN (NOT REPORTED)";
    private final AisService aisService;
    private final GeocodeService geocodeService;

    @Cacheable(cacheNames = POINTS_CACHE_NAME)
    @Override
    public Flux<Point> getPoints() {
        return aisService.fetchLatestShips()
                .switchIfEmpty(error(NoContentException::new))
                .flatMap(this::mapToPoint);
    }

    private Flux<Point> mapToPoint(AisShip aisShip) {

        String mayNullName = aisShip.properties().name() == null ? UNKNOWN_NOT_REPORTED : aisShip.properties().name();
        String mayNullDestination = aisShip.properties().destination() == null ? UNKNOWN_NOT_REPORTED : aisShip.properties().destination();

        return this.getShipDestinationCoordinates(aisShip)
                .map(position ->
                        new Point(
                                aisShip.properties().mmsi(),
                                mayNullName,
                                aisShip.geometry().coordinates().get(X_COORDINATE_INDEX),
                                aisShip.geometry().coordinates().get(Y_COORDINATE_INDEX),
                                mayNullDestination,
                                position.x(),
                                position.y()
                        )
                );
    }

    private Flux<Position> getShipDestinationCoordinates(AisShip aisShip) {
        return geocodeService.getAddressCoordinates(aisShip.properties().destination());
    }
}
