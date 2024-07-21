package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.emailverification.service;

import reactor.core.publisher.Mono;

public interface EmailVerificationService {
    Mono<Void> issueVerificationToken(String uid);

    Mono<Void> verify(String token);
}
