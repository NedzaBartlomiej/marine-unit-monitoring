package pl.bartlomiej.marineunitmonitoring.emailsending;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.RestControllerGlobalErrorHandler;
import reactor.core.publisher.Mono;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(RestControllerGlobalErrorHandler.class);
    private static final String issuer = "Marine-Unit-Monitoring";

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public Mono<Void> sendEmail(String receiverName, String receiverEmail, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(issuer);
        mailMessage.setSubject(receiverName);
        mailMessage.setTo(receiverEmail);
        mailMessage.setText(message);

        return Mono.fromRunnable(() -> javaMailSender.send(mailMessage))
                .doOnError(error -> log.error("Something go wrong on email sending: {}", error.getMessage()))
                .doOnSuccess(result -> log.info("Email sent."))
                .then();
    }
}
