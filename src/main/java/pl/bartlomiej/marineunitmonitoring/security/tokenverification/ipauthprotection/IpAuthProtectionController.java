package pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.dto.VerificationTokenDtoMapper;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.dto.VerificationTokenReadDto;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.service.IpAuthProtectionService;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.OK;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/ip-auth-protection")
public class IpAuthProtectionController {

    private final IpAuthProtectionService ipAuthProtectionService;
    private final VerificationTokenDtoMapper verificationTokenDtoMapper;

    public IpAuthProtectionController(IpAuthProtectionService ipAuthProtectionService, VerificationTokenDtoMapper verificationTokenDtoMapper) {
        this.ipAuthProtectionService = ipAuthProtectionService;
        this.verificationTokenDtoMapper = verificationTokenDtoMapper;
    }

    @GetMapping("/untrusted-authentication/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<VerificationTokenReadDto>>> getUntrustedAuthenticationInfo(@PathVariable String verificationToken) {
        return ipAuthProtectionService.getVerificationToken(verificationToken)
                .map(vt ->
                        buildResponse(
                                OK,
                                buildResponseModel(
                                        null,
                                        OK,
                                        verificationTokenDtoMapper.mapToReadDto(vt),
                                        "verificationToken"
                                )
                        )
                );
    }

    @PatchMapping("/block-account/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> blockAccount(@PathVariable String verificationToken) {
        return ipAuthProtectionService.verify(verificationToken)
                .flatMap(ipAuthProtectionService::blockAccount)
                .then(just(
                        buildResponse(OK,
                                buildResponseModel(
                                        "BLOCKED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }

    @PatchMapping("/trust-ip-address/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> trustIpAddress(@PathVariable String verificationToken) {
        return ipAuthProtectionService.verify(verificationToken)
                .flatMap(ipAuthProtectionService::trustIpAddress)
                .then(just(
                        buildResponse(OK,
                                buildResponseModel(
                                        "TRUSTED",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }
}
