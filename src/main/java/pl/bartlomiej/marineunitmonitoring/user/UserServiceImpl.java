package pl.bartlomiej.marineunitmonitoring.user;

import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.repository.reactive.ReactiveMongoUserRepository;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ReactiveMongoUserRepository reactiveMongoUserRepository;

    @Override
    public Mono<User> getUser(String id) {
        return reactiveMongoUserRepository.findById(id)
                .switchIfEmpty(error(new NotFoundException()));
    }

    @Transactional
    @Override
    public Mono<User> createUser(User user) {
        return reactiveMongoUserRepository.save(user)
                .onErrorResume(throwable -> {
                    if (throwable instanceof DuplicateKeyException) {
                        return error(new UniqueEmailException());
                    }
                    return Mono.error(throwable);
                });
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(String id) {
        return reactiveMongoUserRepository.findById(id)
                .switchIfEmpty(error(new NotFoundException()))
                .flatMap(reactiveMongoUserRepository::delete);
    }
}