package pl.bartlomiej.marineunitmonitoring.map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.bartlomiej.marineunitmonitoring.ais.service.AisService;

@Controller
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final AisService aisService;

    @GetMapping
    public String getMap(Model model) {
        model.addAttribute("points", aisService.getLatestAisPoints());
        return "map";
    }
}
