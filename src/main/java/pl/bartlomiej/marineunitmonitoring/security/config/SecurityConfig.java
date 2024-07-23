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
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers.JWTBlacklistVerifier;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.jwtverifiers.JWTTypeVerifier;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAccessDeniedHandler;
import pl.bartlomiej.marineunitmonitoring.security.exceptionhandling.ResponseModelServerAuthenticationEntryPoint;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final CustomReactiveJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

    public SecurityConfig(CustomReactiveJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter) {
        this.grantedAuthoritiesConverter = grantedAuthoritiesConverter;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  ResponseModelServerAuthenticationEntryPoint authenticationEntryPoint,
                                                  ResponseModelServerAccessDeniedHandler accessDeniedHandler,
                                                  JWTBlacklistVerifier jwtBlacklistVerifier,
                                                  JWTTypeVerifier jwtTypeVerifier) {
        return http
                .httpBasic(HttpBasicSpec::disable)
                .formLogin(FormLoginSpec::disable)
                .logout(LogoutSpec::disable)
                .csrf(CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers(POST, "*/users").permitAll()
                                .pathMatchers(PATCH, "*/users/password/*").permitAll()
                                .pathMatchers(GET, "*/points").permitAll()
                                .pathMatchers(GET, "*/authentication/authenticate").permitAll()
                                .pathMatchers(GET, "*/authentication/verify-email/*").permitAll()
                                .pathMatchers(GET, "*/authentication/initiate-reset-password").permitAll()
                                .pathMatchers(GET, "*/authentication/verify-reset-password/*").permitAll()
                                .anyExchange().authenticated()
                )
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
                .addFilterBefore(jwtBlacklistVerifier, SecurityWebFiltersOrder.AUTHENTICATION)
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