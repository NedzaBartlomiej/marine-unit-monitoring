package pl.bartlomiej.marineunitmonitoring.user.repository;

import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.nested.trackedship.TrackedShip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomUserRepository {

    Mono<TrackedShip> pushTrackedShip(String id, TrackedShip trackedShip);

    Mono<Void> pullTrackedShip(String id, String mmsi);

    Mono<Void> pullTrackedShip(String mmsi);

    Flux<TrackedShip> getTrackedShips(String id);

    Flux<TrackedShip> getTrackedShips();

    Mono<User> findByOpenId(String openId);

    Mono<Void> pushTrustedIpAddress(String id, String ipAddress);
}
