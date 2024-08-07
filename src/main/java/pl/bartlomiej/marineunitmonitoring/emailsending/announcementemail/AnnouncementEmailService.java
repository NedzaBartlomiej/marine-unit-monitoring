package pl.bartlomiej.marineunitmonitoring.emailsending.announcementemail;

import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.AbstractEmailService;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.EmailConstants;
import reactor.core.publisher.Mono;

@Service
public class AnnouncementEmailService extends AbstractEmailService<AnnouncementEmail> {

    public static final String ANNOUNCEMENT_EMAIL_TEMPLATE_PATH = "announcementemailtemplate/index.html";
    private final TemplateEngine templateEngine;

    protected AnnouncementEmailService(JavaMailSender javaMailSender,
                                       TemplateEngine templateEngine,
                                       ResourceLoader resourceLoader) {
        super(javaMailSender, resourceLoader);
        this.templateEngine = templateEngine;
    }

    @Override
    public Mono<Void> sendEmail(final AnnouncementEmail email) {
        return super.processEmailSending(email);
    }

    @Override
    protected String buildHtmlMessage(final AnnouncementEmail email) {
        Context context = new Context();

        context.setVariable(EmailConstants.TITLE, email.getTitle());
        context.setVariable(EmailConstants.MESSAGE, email.getMessage());

        return templateEngine.process(ANNOUNCEMENT_EMAIL_TEMPLATE_PATH, context);
    }
}