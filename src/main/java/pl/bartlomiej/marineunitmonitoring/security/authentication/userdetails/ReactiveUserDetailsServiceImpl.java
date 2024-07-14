package pl.bartlomiej.marineunitmonitoring.security.authentication.userdetails;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserService userService;

    public ReactiveUserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.getUser(username)
                .handle((user, sink) -> {
                    if (user.getPassword() == null) {
                        sink.error(new NotFoundException());
                        return;
                    }
                    sink.next(new SecurityUser(user));
                });
    }
}