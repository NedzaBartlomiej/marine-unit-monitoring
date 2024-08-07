package pl.bartlomiej.marineunitmonitoring.emailsending.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public abstract class AbstractEmailService<T extends Email> implements EmailService<T> {

    private static final String APP_LOGO_PATH = "static/marine-unit-monitoring-app-logo.png";
    private static final String APP_LOGO = "app-logo";
    private static final Logger log = LoggerFactory.getLogger(AbstractEmailService.class);
    private final JavaMailSender javaMailSender;
    private final ResourceLoader resourceLoader;

    protected AbstractEmailService(JavaMailSender javaMailSender, ResourceLoader resourceLoader) {
        this.javaMailSender = javaMailSender;
        this.resourceLoader = resourceLoader;
    }

    protected abstract String buildHtmlMessage(final T email);

    protected MimeMessage buildMimeMessage(final T email) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

        mimeMessageHelper.setTo(email.getReceiverEmail());
        mimeMessageHelper.setSubject(email.getTitle());
        mimeMessageHelper.setText(this.buildHtmlMessage(email), true);

        Resource appLogo = resourceLoader.getResource(
                ResourceLoader.CLASSPATH_URL_PREFIX + APP_LOGO_PATH);
        mimeMessageHelper.addInline(APP_LOGO, appLogo);

        return mimeMessage;
    }

    protected Mono<Void> processEmailSending(final T email) {
        return Mono.fromCallable(() -> buildMimeMessage(email))
                .flatMap(mimeMessage -> Mono.fromRunnable(() -> javaMailSender.send(mimeMessage)))
                .doOnError(error -> log.error("Something go wrong on email sending: {}", error.getMessage()))
                .doOnSuccess(result -> log.info("Email sent."))
                .then();
    }
}