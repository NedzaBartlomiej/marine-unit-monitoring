package pl.bartlomiej.marineunitmonitoring.emailsending.common;

public abstract class Email {
    private final String receiverEmail;
    private final String title;
    private final String message;

    public Email(String receiverEmail, String title, String message) {
        this.receiverEmail = receiverEmail;
        this.title = title;
        this.message = message;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
