package pl.bartlomiej.marineunitmonitoring.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.user.repository.reactive.ReactiveMongoUserRepository;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final ReactiveMongoUserRepository reactiveMongoUserRepository;

    public ReactiveUserDetailsServiceImpl(ReactiveMongoUserRepository reactiveMongoUserRepository) {
        this.reactiveMongoUserRepository = reactiveMongoUserRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return reactiveMongoUserRepository.findByEmail(username)
                .map(SecurityUser::new);
    }
}
