package pl.bartlomiej.marineunitmonitoring.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.ActivePoint;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.InactivePointFilter;
import pl.bartlomiej.marineunitmonitoring.point.activepoint.service.ActivePointService;
import reactor.core.publisher.Flux;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/v1/points")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;
    private final ActivePointService activePointService;
    private final InactivePointFilter inactivePointFilter;

    public PointController(
            PointService pointService,
            ActivePointService activePointService,
            InactivePointFilter inactivePointFilter) {
        this.pointService = pointService;
        this.activePointService = activePointService;
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
                                activePointService.addActivePoint(
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
                                        .body(of("point", point))
                                        .build()
                        )
        );
    }

}
