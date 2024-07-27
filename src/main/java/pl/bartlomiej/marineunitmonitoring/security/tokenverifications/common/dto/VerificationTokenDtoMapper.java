package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.dto;

import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.common.util.MapperUtil;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;

@Component
public class VerificationTokenDtoMapper {

    public VerificationTokenReadDto mapToReadDto(VerificationToken verificationToken) {
        return MapperUtil.copyProperties(verificationToken, VerificationTokenReadDto.class);
    }
}
