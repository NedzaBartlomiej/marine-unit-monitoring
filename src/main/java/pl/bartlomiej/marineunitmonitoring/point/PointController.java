package pl.bartlomiej.marineunitmonitoring.point;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.InactivePointFilter;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointAsyncService;
import reactor.core.publisher.Flux;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;
    private final ActivePointAsyncService activePointAsyncService;
    private final InactivePointFilter inactivePointFilter;

    public PointController(
            PointService pointService,
            @Qualifier("activePointAsyncServiceImpl") ActivePointAsyncService activePointAsyncService,
            InactivePointFilter inactivePointFilter) {
        this.pointService = pointService;
        this.activePointAsyncService = activePointAsyncService;
        this.inactivePointFilter = inactivePointFilter;
    }

    @GetMapping
    public ResponseEntity<Flux<ResponseModel<Point>>> getPoints() {

        // ACTIVE LIST FILTRATION
        pointService.getPoints()
                .map(Point::mmsi)
                .collectList()
                .subscribe(mmsis ->
                        inactivePointFilter.filter(mmsis).subscribe()
                );

        // RESPONSE
        return ResponseEntity.ok(
                pointService.getPoints()
                        .flatMap(point ->
                                activePointAsyncService.addActivePoint(
                                        new ActivePoint(
                                                point.mmsi(),
                                                point.name()
                                        )
                                ).thenReturn(point)
                        )
                        .map(point ->
                                ResponseModel.<Point>builder()
                                        .httpStatus(OK)
                                        .httpStatusCode(OK.value())
                                        .body(of("Point", point))
                                        .build()
                        )
        );
    }

}
