package pl.bartlomiej.marineunitmonitoring.user.service.reactive;

import com.mongodb.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.common.error.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.reactive.ReactiveMongoUserRepository;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@Service
public class ReactiveUserServiceImpl implements ReactiveUserService {

    private final ReactiveMongoUserRepository reactiveMongoUserRepository;

    public ReactiveUserServiceImpl(ReactiveMongoUserRepository reactiveMongoUserRepository) {
        this.reactiveMongoUserRepository = reactiveMongoUserRepository;
    }

    @Override
    public Mono<User> getUserById(String id) {
        return reactiveMongoUserRepository.findById(id)
                .switchIfEmpty(error(new NotFoundException()));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return reactiveMongoUserRepository.findByEmail(email)
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
