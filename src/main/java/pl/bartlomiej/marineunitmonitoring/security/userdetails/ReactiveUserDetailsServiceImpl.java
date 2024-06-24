package pl.bartlomiej.marineunitmonitoring.security.userdetails;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final MongoUserRepository mongoUserRepository;

    public ReactiveUserDetailsServiceImpl(MongoUserRepository mongoUserRepository) {
        this.mongoUserRepository = mongoUserRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return mongoUserRepository.findByEmail(username)
                .map(SecurityUser::new);
    }
}
