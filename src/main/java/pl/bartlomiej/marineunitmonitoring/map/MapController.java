package pl.bartlomiej.marineunitmonitoring.map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.ais.service.AisService;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class MapController {

    private final AisService aisService;

    @GetMapping(value = "/points", produces = "text/event-stream")
    public Flux<ServerSentEvent<Point>> getPoints() {
        return aisService.getLatestAisPoints()
                .map(point -> ServerSentEvent.builder(point).build());
    }
}
