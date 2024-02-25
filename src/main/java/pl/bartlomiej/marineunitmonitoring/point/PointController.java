package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.ais.service.AisService;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final AisService aisService;

    @GetMapping(value = "/points", produces = APPLICATION_NDJSON_VALUE)
    public Flux<Point> getPoints() {
        return aisService.getLatestAisPoints();
    }
}
