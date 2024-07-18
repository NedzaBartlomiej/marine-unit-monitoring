package pl.bartlomiej.marineunitmonitoring.emailsending;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Void> sendEmail(String receiverEmail, String title, String message);
}
