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

    public <T extends VerificationTokenDto> T map(VerificationToken verificationToken, Class<T> dtoClass) {
        return modelMapper.map(verificationToken, dtoClass);
    }

    public <T extends VerificationToken> T map(VerificationTokenDto verificationTokenDto, Class<T> verificationTokenClass) {
        return modelMapper.map(verificationTokenDto, verificationTokenClass);
    }
}