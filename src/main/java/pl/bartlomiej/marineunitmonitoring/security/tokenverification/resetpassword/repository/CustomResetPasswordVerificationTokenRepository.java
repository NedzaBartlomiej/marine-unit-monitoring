package pl.bartlomiej.marineunitmonitoring.security.tokenverification.resetpassword.repository;

import reactor.core.publisher.Mono;

public interface CustomResetPasswordVerificationTokenRepository {
    Mono<Void> updateIsVerified(String id, boolean isVerified);
}
