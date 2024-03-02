package pl.bartlomiej.marineunitmonitoring.point;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.ais.AisService;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    private final AisService aisService;

    @GetMapping
    public Flux<Point> getPoints() {
        return aisService.getLatestAisPoints();
    }
}
