package pl.bartlomiej.marineunitmonitoring.emailsending.common;

import reactor.core.publisher.Mono;

public interface EmailService<T extends Email> {
    Mono<Void> sendEmail(T email);
}
