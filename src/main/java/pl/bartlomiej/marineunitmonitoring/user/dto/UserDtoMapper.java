package pl.bartlomiej.marineunitmonitoring.user.dto;

import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.user.User;

import static pl.bartlomiej.marineunitmonitoring.common.util.MapperUtils.copyProperties;

@Component
public class UserDtoMapper {

    public User mapFrom(UserSaveDto userSaveDto) {
        return copyProperties(userSaveDto, User.class);
    }

    public UserReadDto mapToReadDto(User user) {
        return copyProperties(user, UserReadDto.class);
    }
}
