package pl.bartlomiej.marineunitmonitoring.emailsending.verificationemail;

import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.AbstractEmailService;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.EmailConstants;
import reactor.core.publisher.Mono;

@Service
public class VerificationEmailService extends AbstractEmailService<VerificationEmail> {

    private static final String VERIFICATION_BUTTON_TEXT = "verificationButtonText";
    private static final String VERIFICATION_LINK = "verificationLink";
    private static final String VERIFICATION_EMAIL_TEMPLATE_PATH = "verificationemailtemplate/index.html";
    private final TemplateEngine templateEngine;

    public VerificationEmailService(JavaMailSender javaMailSender,
                                    TemplateEngine templateEngine,
                                    ResourceLoader resourceLoader) {
        super(javaMailSender, resourceLoader);
        this.templateEngine = templateEngine;
    }

    @Override
    public Mono<Void> sendEmail(final VerificationEmail verificationEmail) {
        return super.processEmailSending(verificationEmail);
    }

    @Override
    protected String buildHtmlMessage(final VerificationEmail email) {
        Context context = new Context();

        context.setVariable(EmailConstants.TITLE, email.getTitle());
        context.setVariable(EmailConstants.MESSAGE, email.getMessage());
        context.setVariable(VERIFICATION_LINK, email.getVerificationLink());
        context.setVariable(VERIFICATION_BUTTON_TEXT, email.getVerificationButtonText());

        return templateEngine.process(VERIFICATION_EMAIL_TEMPLATE_PATH, context);
    }
}