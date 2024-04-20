package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.ResponseModel;
import reactor.core.publisher.Flux;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;
    private final ActivePointsManager activePointsManager;

    @GetMapping
    public ResponseEntity<Flux<ResponseModel<Point>>> getPoints() {

        Flux<Point> pointFlux = pointService.getPoints().cache();

        // ACTIVE LIST FILTRATION todo - fix - przez wykonanie tego wystepuje cast exception ArrayList -> Point
        pointFlux.map(Point::mmsi).collectList()
                .subscribe(activePointsManager::filterInactiveShips);

        // RESPONSE
        return ResponseEntity.ok(
                pointFlux
                        .doOnNext(point ->
                                activePointsManager.addActivePoint(
                                        new ActivePointsManager.ActivePoint(
                                                point.mmsi(),
                                                point.name()
                                        )
                                )
                        )
                        .map(response ->
                                ResponseModel.<Point>builder()
                                        .httpStatus(OK)
                                        .httpStatusCode(OK.value())
                                        .body(of("Point", response))
                                        .build()
                        )
        );
    }

}
