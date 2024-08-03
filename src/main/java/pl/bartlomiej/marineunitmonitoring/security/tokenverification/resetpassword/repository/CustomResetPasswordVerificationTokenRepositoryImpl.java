package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword.repository;

import org.springframework.stereotype.Repository;
import pl.bartlomiej.marineunitmonitoring.common.helper.repository.CustomRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenConstants;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword.ResetPasswordVerificationToken;
import reactor.core.publisher.Mono;

@Repository
public class CustomResetPasswordVerificationTokenRepositoryImpl implements CustomResetPasswordVerificationTokenRepository {

    private final CustomRepository customRepository;

    public CustomResetPasswordVerificationTokenRepositoryImpl(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    @Override
    public Mono<Void> updateIsVerified(String id, boolean isVerified) {
        return customRepository.updateOne(id, VerificationTokenConstants.IS_VERIFIED, isVerified, ResetPasswordVerificationToken.class)
                .then();
    }
}
