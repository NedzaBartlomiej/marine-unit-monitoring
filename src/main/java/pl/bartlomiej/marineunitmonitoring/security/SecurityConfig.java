package pl.bartlomiej.marineunitmonitoring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import pl.bartlomiej.marineunitmonitoring.security.webfilters.OAuth2JwtWebFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final OAuth2JwtWebFilter oAuth2JwtWebFilter;
    private final CustomJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

    public SecurityConfig(OAuth2JwtWebFilter oAuth2JwtWebFilter, CustomJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter) {
        this.oAuth2JwtWebFilter = oAuth2JwtWebFilter;
        this.grantedAuthoritiesConverter = grantedAuthoritiesConverter;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic(HttpBasicSpec::disable)
                .csrf(CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers(GET, "/points").permitAll()
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oAuth2ResourceServerSpec ->
                        oAuth2ResourceServerSpec.jwt(jwtSpec ->
                                jwtSpec.jwtAuthenticationConverter(getJwtAuthenticationConverter())
                        )
                )
                .addFilterBefore(oAuth2JwtWebFilter, AUTHENTICATION)
                .build();
    }

    // set this in filter chain
    @Bean
    ReactiveJwtAuthenticationConverter getJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                this.reactiveJwtGrantedAuthoritiesConverterAdapter()
        );
        return jwtAuthenticationConverter;
    }

    @Bean
    ReactiveJwtGrantedAuthoritiesConverterAdapter reactiveJwtGrantedAuthoritiesConverterAdapter() {
        return new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter);
    }
}
