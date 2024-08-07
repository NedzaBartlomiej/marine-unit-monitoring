package pl.bartlomiej.marineunitmonitoring.announcement;

import reactor.core.publisher.Mono;

public interface AnnouncementService {
    Mono<Void> announce(Announcement announcement);
}
