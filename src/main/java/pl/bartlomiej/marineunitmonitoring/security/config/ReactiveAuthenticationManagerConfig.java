package pl.bartlomiej.marineunitmonitoring.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.bartlomiej.marineunitmonitoring.security.authentication.userdetails.ReactiveUserDetailsServiceImpl;

@Configuration
public class ReactiveAuthenticationManagerConfig {

    @Bean
    ReactiveAuthenticationManager userDetailsReactiveAuthenticationManager(
            final ReactiveUserDetailsServiceImpl userDetailsService,
            final BCryptPasswordEncoder passwordEncoder) {
        var manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }
}
