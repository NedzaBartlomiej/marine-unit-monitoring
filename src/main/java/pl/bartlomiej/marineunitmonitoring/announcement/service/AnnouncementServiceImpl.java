package pl.bartlomiej.marineunitmonitoring.announcement.service;

import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.announcement.Announcement;
import pl.bartlomiej.marineunitmonitoring.announcement.AnnouncementService;
import pl.bartlomiej.marineunitmonitoring.emailsending.announcementemail.AnnouncementEmail;
import pl.bartlomiej.marineunitmonitoring.emailsending.common.EmailService;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final UserService userService;
    private final EmailService<AnnouncementEmail> emailService;

    public AnnouncementServiceImpl(UserService userService, EmailService<AnnouncementEmail> emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public Mono<Void> announce(final Announcement announcement) {
        return userService.getAllEmails()
                .flatMap(email -> emailService.sendEmail(
                        new AnnouncementEmail(
                                email,
                                announcement.title(),
                                announcement.message()
                        ))
                ).then();
    }
}