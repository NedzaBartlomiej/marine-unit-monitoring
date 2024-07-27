package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword.service.ResetPasswordService;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.OK;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/reset-password")
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    public ResetPasswordController(ResetPasswordService resetPasswordService) {
        this.resetPasswordService = resetPasswordService;
    }

    @GetMapping("/initiate")
    public Mono<ResponseEntity<ResponseModel<Void>>> initiateResetPassword(@RequestBody String email) {
        return resetPasswordService.issue(email, null)
                .then(just(
                        buildResponse(OK,
                                buildResponseModel(
                                        "EMAIL_SENT",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }

    @GetMapping("/verify/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> verifyResetPassword(@PathVariable String verificationToken) {
        return resetPasswordService.verify(verificationToken)
                .flatMap(resetPasswordService::performVerifiedTokenAction)
                .then(just(
                        buildResponse(OK,
                                buildResponseModel(
                                        "VERIFIED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }

    @PatchMapping("/reset/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> resetPassword(@PathVariable String verificationToken, @RequestBody String newPassword) {
        return resetPasswordService.processResetPassword(verificationToken, newPassword)
                .then(just(
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        "CHANGED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }
}
