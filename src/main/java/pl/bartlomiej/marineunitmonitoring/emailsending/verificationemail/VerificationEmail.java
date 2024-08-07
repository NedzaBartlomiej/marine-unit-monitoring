package pl.bartlomiej.marineunitmonitoring.emailsending.verificationemail;

import pl.bartlomiej.marineunitmonitoring.emailsending.common.Email;

public class VerificationEmail extends Email {

    private final String verificationLink;
    private final String verificationButtonText;

    public VerificationEmail(String receiverEmail, String title, String message, String verificationLink, String verificationButtonText) {
        super(receiverEmail, title, message);
        this.verificationLink = verificationLink;
        this.verificationButtonText = verificationButtonText;
    }

    public String getVerificationLink() {
        return verificationLink;
    }

    public String getVerificationButtonText() {
        return verificationButtonText;
    }
}
