package pl.bartlomiej.marineunitmonitoring.security.authentication;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import pl.bartlomiej.marineunitmonitoring.common.helper.ResponseModel;
import pl.bartlomiej.marineunitmonitoring.common.util.ControllerResponseUtil;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.authentication.service.AuthenticationService;
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

    public AuthenticationController(AuthenticationService authenticationService, UserService userService, JWTService jwtService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/authenticate")
    public Mono<ResponseEntity<ResponseModel<Map<String, String>>>> authenticate(@RequestBody @Valid UserAuthDto userAuthDto) {
        return userService.getUserByEmail(userAuthDto.getEmail())
                .flatMap(user -> authenticationService.authenticate(
                                        user.getId(), userAuthDto.getEmail(), userAuthDto.getPassword()
                                )
                                .map(tokensMap ->
                                        ControllerResponseUtil.buildResponse(
                                                OK,
                                                ControllerResponseUtil.buildResponseModel(
                                                        "Authenticated successfully.",
                                                        OK,
                                                        tokensMap,
                                                        "authenticationTokens"
                                                )
                                        )
                                )
                );
    }

    // GET - refreshAccessToken(String refreshToken)

    @PreAuthorize("hasRole(T(pl.bartlomiej.marineunitmonitoring.user.nested.Role).SIGNED.name())")
    @GetMapping("/invalidateToken")
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
}
