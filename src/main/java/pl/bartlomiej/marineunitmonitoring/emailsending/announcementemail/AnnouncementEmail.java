package pl.bartlomiej.marineunitmonitoring.emailsending.announcementemail;

import pl.bartlomiej.marineunitmonitoring.emailsending.common.Email;

public class AnnouncementEmail extends Email {
    public AnnouncementEmail(String receiverEmail, String title, String message) {
        super(receiverEmail, title, message);
    }
}
