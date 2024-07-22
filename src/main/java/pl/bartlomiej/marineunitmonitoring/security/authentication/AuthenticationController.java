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
import pl.bartlomiej.marineunitmonitoring.user.User;
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
    private final VerificationTokenService<User, String> resetPasswordService;
    private final VerificationTokenService<Void, String> emailVerificationService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    UserService userService,
                                    JWTService jwtService,
                                    @Qualifier("resetPasswordService") VerificationTokenService<User, String> resetPasswordService,
                                    @Qualifier("emailVerificationService") VerificationTokenService<Void, String> emailVerificationService1) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.resetPasswordService = resetPasswordService;
        this.emailVerificationService = emailVerificationService1;
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
                                                        "Authenticated successfully.",
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
                                "Token has been refreshed successfully. Refresh token has been rotated.",
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
                                "Token has been successfully invalidated.",
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
                                        "Email has been verified successfully.",
                                        OK,
                                        null,
                                        null
                                )
                        )
                ));
    }

    @GetMapping("/reset-password")
    public Mono<ResponseEntity<ResponseModel<Void>>> resetPassword(@RequestBody String email) {
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
}
