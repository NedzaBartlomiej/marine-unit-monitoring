package pl.bartlomiej.marineunitmonitoring.security.authentication;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.authentication.service.AuthenticationService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.user.dto.UserAuthDto;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponse;
import static pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil.buildResponseModel;
import static reactor.core.publisher.Mono.just;

@RestController
@RequestMapping("/v1/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JWTService jwtService;
    private final VerificationTokenService resetPasswordService;
    private final VerificationTokenService emailVerificationService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    UserService userService,
                                    JWTService jwtService,
                                    @Qualifier("resetPasswordService") VerificationTokenService resetPasswordService,
                                    @Qualifier("emailVerificationService") VerificationTokenService emailVerificationService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.resetPasswordService = resetPasswordService;
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/authenticate")
    public Mono<ResponseEntity<ResponseModel<Map<String, String>>>> authenticate(@RequestBody @Valid UserAuthDto userAuthDto) {
        return userService.getUserByEmail(userAuthDto.getEmail())
                .flatMap(user -> authenticationService.authenticate(
                                        user.getId(), userAuthDto.getEmail(), userAuthDto.getPassword()
                                )
                                .map(tokens ->
                                        ControllerResponseUtil.buildResponse(
                                                OK,
                                                ControllerResponseUtil.buildResponseModel(
                                                        "AUTHENTICATED",
                                                        OK,
                                                        tokens,
                                                        "authenticationTokens"
                                                )
                                        )
                                )
                );
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping("/refresh-access-token")
    public Mono<ResponseEntity<ResponseModel<Map<String, String>>>> refreshAccessToken(ServerWebExchange exchange) {
        return jwtService.refreshAccessToken(
                jwtService.extract(exchange)
        ).map(tokens ->
                buildResponse(
                        OK,
                        buildResponseModel(
                                "REFRESHED,REFRESH_TOKEN_ROTATED",
                                OK,
                                tokens,
                                "authenticationTokens"
                        )
                )
        );
    }

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping("/invalidate-token")
    public Mono<ResponseEntity<ResponseModel<Void>>> invalidateToken(ServerWebExchange exchange) {
        return jwtService.invalidate(
                jwtService.extract(exchange)
        ).then(just(
                buildResponse(
                        OK,
                        buildResponseModel(
                                "INVALIDATED",
                                OK,
                                null,
                                null
                        )
                )
        ));
    }

    @GetMapping("/verify-email/{token}")
    public Mono<ResponseEntity<ResponseModel<Void>>> verifyEmail(@PathVariable String token) {
        return emailVerificationService.verify(token)
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

    @GetMapping("/initiate-reset-password")
    public Mono<ResponseEntity<ResponseModel<Void>>> initiateResetPassword(@RequestBody String email) {
        return resetPasswordService.issue(email)
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

    @GetMapping("/verify-reset-password/{verificationToken}")
    public Mono<ResponseEntity<ResponseModel<Void>>> verifyResetPassword(@PathVariable String verificationToken) {
        return resetPasswordService.verify(verificationToken)
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
}
