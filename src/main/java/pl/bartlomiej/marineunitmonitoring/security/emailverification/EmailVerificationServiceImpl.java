package pl.bartlomiej.marineunitmonitoring.security.emailverification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.AccountAlreadyVerifiedException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final MongoEmailVerificationEntityRepository emailVerificationEntityRepository;
    private final UserService userService;

    public EmailVerificationServiceImpl(MongoEmailVerificationEntityRepository emailVerificationEntityRepository, UserService userService) {
        this.emailVerificationEntityRepository = emailVerificationEntityRepository;
        this.userService = userService;
    }

    @Override
    public Mono<Void> issueVerificationToken(String uid) {
        return userService.getUser(uid)
                .switchIfEmpty(error(NotFoundException::new))
                .flatMap(user -> emailVerificationEntityRepository.save(
                        new EmailVerificationEntity(uid))
                )
                .then();
    }

    @Override
    public Mono<Void> verify(String token) {
        return emailVerificationEntityRepository.findById(token)
                .switchIfEmpty(error(AccountAlreadyVerifiedException::new))
                .flatMap(emailVerificationEntity -> userService.getUser(emailVerificationEntity.getUid()))
                .flatMap(user -> userService.verifyUser(user.getId()))
                .then(emailVerificationEntityRepository.deleteById(token));
    }

    // todo - clearAbandonedVerificationIngredients @Scheduled
}
