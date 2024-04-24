package pl.bartlomiej.marineunitmonitoring.shiptracking.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
final public class DateRangeHelper {
    private LocalDateTime from;
    private LocalDateTime to;
}
