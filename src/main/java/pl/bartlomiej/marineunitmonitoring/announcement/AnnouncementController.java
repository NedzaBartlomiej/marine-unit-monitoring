package pl.bartlomiej.marineunitmonitoring.announcement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.OK;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).ADMIN.name())")
    @GetMapping("/announce")
    public Mono<ResponseEntity<ResponseModel<Void>>> announce(@RequestBody Announcement announcement) {
        return announcementService.announce(announcement)
                .then(just(
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        "ANNOUNCED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }
}
