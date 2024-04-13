package pl.bartlomiej.marineunitmonitoring.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class DateRange {
    private LocalDateTime from;
    private LocalDateTime to;
}
