package pl.bartlomiej.marineunitmonitoring.user.dto;

import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.common.util.MapperUtil;
import pl.bartlomiej.marineunitmonitoring.user.User;

@Component
public class UserDtoMapper {

    public User mapFrom(UserSaveDto userSaveDto) {
        return MapperUtil.copyProperties(userSaveDto, User.class);
    }

    public UserReadDto mapToReadDto(User user) {
        return MapperUtil.copyProperties(user, UserReadDto.class);
    }
}
