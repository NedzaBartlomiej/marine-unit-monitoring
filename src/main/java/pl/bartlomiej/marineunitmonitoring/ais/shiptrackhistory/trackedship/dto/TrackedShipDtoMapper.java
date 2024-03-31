package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto;

import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;

import static pl.bartlomiej.marineunitmonitoring.common.util.DtoUtils.copyProperties;

@Component
public class TrackedShipDtoMapper {

    public TrackedShip mapFrom(TrackedShipReadDto dto) {
        return copyProperties(dto, TrackedShip.class);
    }

    public TrackedShip mapFrom(TrackedShipSaveDto dto, String name) {
        dto.setName(name);
        return copyProperties(dto, TrackedShip.class);
    }

    public TrackedShipReadDto mapToReadDto(TrackedShip trackedShip) {
        return copyProperties(trackedShip, TrackedShipReadDto.class);
    }
}
