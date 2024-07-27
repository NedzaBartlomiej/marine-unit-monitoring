package pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;

@Component
public class VerificationTokenDtoMapper {

    private final ModelMapper modelMapper;

    public VerificationTokenDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public VerificationTokenReadDto mapToReadDto(VerificationToken verificationToken) {
        return modelMapper.map(verificationToken, VerificationTokenReadDto.class);
    }
}
