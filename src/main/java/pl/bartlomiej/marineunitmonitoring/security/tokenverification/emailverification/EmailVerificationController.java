package pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.emailverification.service.EmailVerificationService;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.OK;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/email-verification")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/verify/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> verifyEmail(@PathVariable String verificationToken) {
        return emailVerificationService.verify(verificationToken)
                .flatMap(emailVerificationService::performVerifiedTokenAction)
                .then(just(
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        "VERIFIED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }
}
