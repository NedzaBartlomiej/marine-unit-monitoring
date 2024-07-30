package pl.bartlomiej.marineunitmonitoring.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.LogoutSpec;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import pl.bartlomiej.marineunitmonitoring.security.authentication.grantedauthorities.CustomReactiveJwtGrantedAuthoritiesConverter;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers.JWTValidityVerifier;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers.JWTTypeVerifier;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAccessDeniedHandler;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAuthenticationEntryPoint;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final CustomReactiveJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
    private final List<String> postEndpoints = List.of(
            "*/users"
    );
    private final List<String> getEndpoints = List.of(
            "*/points",
            "*/authentication/authenticate",
            "*/email-verification/verify/*",
            "*/reset-password/initiate",
            "*/reset-password/verify/*",
            "*/ip-auth-protection/untrusted-authentication/*"
    );
    private final List<String> patchEndpoints = List.of(
            "*/reset-password/reset/*",
            "*/ip-auth-protection/block-account/*",
            "*/ip-auth-protection/trust-ip-address/*"
    );

    public SecurityConfig(CustomReactiveJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter) {
        this.grantedAuthoritiesConverter = grantedAuthoritiesConverter;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  ResponseModelServerAuthenticationEntryPoint authenticationEntryPoint,
                                                  ResponseModelServerAccessDeniedHandler accessDeniedHandler,
                                                  JWTValidityVerifier jwtValidityVerifier,
                                                  JWTTypeVerifier jwtTypeVerifier) {
        return http
                .httpBasic(HttpBasicSpec::disable)
                .formLogin(FormLoginSpec::disable)
                .logout(LogoutSpec::disable)
                .csrf(CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth -> {
                    postEndpoints.forEach(e -> auth.pathMatchers(POST, e).permitAll());
                    getEndpoints.forEach(e -> auth.pathMatchers(GET, e).permitAll());
                    patchEndpoints.forEach(e -> auth.pathMatchers(PATCH, e).permitAll());
                    auth.anyExchange().authenticated();
                })
                .oauth2ResourceServer(oAuth2ResourceServerSpec ->
                        oAuth2ResourceServerSpec
                                .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint)
                )
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(jwtValidityVerifier, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(jwtTypeVerifier, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}