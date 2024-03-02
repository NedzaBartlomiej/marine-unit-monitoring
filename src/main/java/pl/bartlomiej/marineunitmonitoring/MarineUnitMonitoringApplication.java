package pl.bartlomiej.marineunitmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarineUnitMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarineUnitMonitoringApplication.class, args);
    }

}
