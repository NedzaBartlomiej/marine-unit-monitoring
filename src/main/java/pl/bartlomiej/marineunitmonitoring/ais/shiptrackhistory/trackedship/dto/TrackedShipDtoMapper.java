package pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.dto;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.ais.shiptrackhistory.trackedship.TrackedShip;

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

    @SneakyThrows
    private <T> T copyProperties(Object source, Class<T> targetClass) {
        T target = targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
