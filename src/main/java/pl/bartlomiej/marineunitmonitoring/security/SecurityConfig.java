package pl.bartlomiej.marineunitmonitoring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import pl.bartlomiej.marineunitmonitoring.security.webfilters.OAuth2JwtWebFilter;
import pl.bartlomiej.marineunitmonitoring.user.service.reactive.ReactiveUserService;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final OAuth2JwtWebFilter oAuth2JwtWebFilter;
    private final ReactiveUserService reactiveUserService;

    public SecurityConfig(OAuth2JwtWebFilter oAuth2JwtWebFilter, ReactiveUserService reactiveUserService) {
        this.oAuth2JwtWebFilter = oAuth2JwtWebFilter;
        this.reactiveUserService = reactiveUserService;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers(GET, "/points").permitAll()
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults())
                )
                .addFilterBefore(oAuth2JwtWebFilter, AUTHENTICATION)
                .build();
    }

    // set this in filter chain
//    @Bean
//    Converter<Jwt, Mono<AbstractAuthenticationToken>> getJwtAuthenticationConverter() {
//        Mono<JwtAuthenticationConverter> converterMono = just(new JwtAuthenticationConverter());
//        return converterMono
//                .map(jwtAuthenticationConverter ->
//                        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtSource -> {
//                            return
//                        })
//                );
//    }
}
