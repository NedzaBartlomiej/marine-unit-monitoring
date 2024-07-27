package pl.bartlomiej.marineunitmonitoring.user.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.user.User;

@Component
public class UserDtoMapper {

    private final ModelMapper modelMapper;

    public UserDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User mapFrom(UserSaveDto userSaveDto) {
        return modelMapper.map(userSaveDto, User.class);
    }

    public UserReadDto mapToReadDto(User user) {
        return modelMapper.map(user, UserReadDto.class);
    }
}
