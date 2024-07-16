package pl.bartlomiej.marineunitmonitoring.shiptracking.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bartlomiej.marineunitmonitoring.common.error.RestControllerGlobalErrorHandler;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;

public record DateRangeHelper(LocalDateTime from, LocalDateTime to) {

    private static final Logger log = LoggerFactory.getLogger(RestControllerGlobalErrorHandler.class);

    public DateRangeHelper(LocalDateTime from, LocalDateTime to) {
        log.info("Processing date range: [{}] - [{}]", from, to);
        this.from = (from == null) ? of(0, 1, 1, 0, 0, 0) : from;
        this.to = (to == null) ? now() : to;
        log.info("Processed date range: [{}] - [{}]", from, to);
    }
}
