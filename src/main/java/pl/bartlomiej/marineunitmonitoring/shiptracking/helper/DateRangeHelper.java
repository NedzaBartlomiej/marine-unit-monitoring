package pl.bartlomiej.marineunitmonitoring.shiptracking.helper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;

@Getter
@Slf4j
public record DateRangeHelper(LocalDateTime from, LocalDateTime to) {
    public DateRangeHelper(LocalDateTime from, LocalDateTime to) {
        log.info("Processing date range: [{}] - [{}]", from, to);
        this.from = (from == null) ? of(0, 1, 1, 0, 0, 0) : from;
        this.to = (to == null) ? now() : to;
        log.info("Processed date range: [{}] - [{}]", from, to);
    }
}
