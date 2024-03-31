package pl.bartlomiej.marineunitmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableReactiveMongoRepositories
@EnableMongoRepositories
public class MarineUnitMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarineUnitMonitoringApplication.class, args);
    }

}
