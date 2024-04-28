package pl.bartlomiej.marineunitmonitoring.point;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.manager.ActivePointManager;
import reactor.core.publisher.Flux;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;
    private final ActivePointManager activePointManager;

    public PointController(
            PointService pointService,
            @Qualifier("activePointAsyncManager") ActivePointManager activePointManager) {
        this.pointService = pointService;
        this.activePointManager = activePointManager;
    }

    @GetMapping
    public ResponseEntity<Flux<ResponseModel<Point>>> getPoints() {

        // ACTIVE LIST FILTRATION
        pointService.getPoints()
                .map(Point::mmsi)
                .collectList()
                .subscribe(activePointManager::filterInactiveShips);

        // RESPONSE
        return ResponseEntity.ok(
                pointService.getPoints()
                        .doOnNext(point ->
                                activePointManager.addActivePoint(
                                        new ActivePoint(
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
