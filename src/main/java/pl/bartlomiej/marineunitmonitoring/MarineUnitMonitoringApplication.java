package pl.bartlomiej.marineunitmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwskeyselector.config.properties.MultiProvidersJWSKeySelectorProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MultiProvidersJWSKeySelectorProperties.class)
public class MarineUnitMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarineUnitMonitoringApplication.class, args);
    }

}
