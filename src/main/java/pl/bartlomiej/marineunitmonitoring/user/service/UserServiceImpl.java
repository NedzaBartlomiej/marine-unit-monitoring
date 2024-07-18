package pl.bartlomiej.marineunitmonitoring.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.common.error.RestControllerGlobalErrorHandler;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.NotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.apiexceptions.UniqueEmailException;
import pl.bartlomiej.marineunitmonitoring.common.error.authexceptions.RegisterBasedUserNotFoundException;
import pl.bartlomiej.marineunitmonitoring.common.error.authexceptions.UnverifiedAccountException;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTServiceImpl;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.repository.CustomUserRepository;
import pl.bartlomiej.marineunitmonitoring.user.repository.MongoUserRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static java.util.List.of;
import static pl.bartlomiej.marineunitmonitoring.user.nested.Role.SIGNED;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(RestControllerGlobalErrorHandler.class);
    private final CustomUserRepository customUserRepository;
    private final MongoUserRepository mongoUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(CustomUserRepository customUserRepository, MongoUserRepository mongoUserRepository, BCryptPasswordEncoder passwordEncoder) {
        this.customUserRepository = customUserRepository;
        this.mongoUserRepository = mongoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> getUser(String id) {
        return mongoUserRepository.findById(id)
                .switchIfEmpty(error(NotFoundException::new));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return mongoUserRepository.findByEmail(email)
                .switchIfEmpty(error(NotFoundException::new));
    }

    @Override
    public Mono<User> createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(of(SIGNED));
        return mongoUserRepository.save(user)
                .onErrorResume(throwable -> {
                    if (throwable instanceof DuplicateKeyException) {
                        log.error(UniqueEmailException.MESSAGE);
                        return error(UniqueEmailException::new);
                    } else {
                        return error(throwable);
                    }
                });
    }

    @Override
    public Mono<Void> verifyUser(String id) {
        return customUserRepository.updateIsVerified(id, true);
    }

    @Override
    public Mono<Void> deleteUser(String id) {
        return this.isUserExists(id)
                .flatMap(exists -> mongoUserRepository.deleteById(id));
    }

    public Mono<Boolean> isUserExists(String id) {
        return mongoUserRepository.existsById(id)
                .flatMap(exists -> exists ? just(true) : error(NotFoundException::new));
    }

    @Override
    public Mono<String> identifyUser(String subjectId) {
        return mongoUserRepository.findById(subjectId)
                .switchIfEmpty(customUserRepository.findByOpenId(subjectId))
                .map(User::getId)
                .switchIfEmpty(error(NotFoundException::new));
    }

    @Override
    public Mono<User> processAuthenticationFlowUser(String id, String username, String email, String tokenIssuer) {
        log.info("Processing authentication flow user.");
        return mongoUserRepository.findByEmail(email)
                .flatMap(this::isUserVerified)
                .flatMap(user -> this.processUserOpenIds(user, id))
                .switchIfEmpty(this.processUserCreation(id, username, email, tokenIssuer));
    }

    private Mono<User> isUserVerified(User user) {
        return just(user)
                .map(User::getVerified)
                .flatMap(isVerified -> isVerified
                        ? just(user)
                        : error(UnverifiedAccountException::new) // todo - set custom failureHandler in AuthWebFilter to properly handle exception with my custom handler
                );
    }

    private Mono<User> processUserOpenIds(User user, String id) {
        log.info("Processing authentication flow user openid cases.");
        if (id.equals(user.getId())) {
            log.info("Registration based user detected, no adding openid.");
            return Mono.just(user);
        }
        if (!user.getOpenIds().contains(id)) {
            log.info("Adding new openid for user.");
            this.addOpenId(user, id);
            return mongoUserRepository.save(user);
        }
        log.info("Existing openid, returning user.");
        return Mono.just(user);
    }

    private void addOpenId(User user, String openId) {
        if (user.getOpenIds() == null) {
            user.setOpenIds(new ArrayList<>());
        }
        user.getOpenIds().add(openId);
    }

    private Mono<User> processUserCreation(String id, String username, String email, String tokenIssuer) {
        log.info("Processing authentication flow user creation.");
        if (tokenIssuer.equals(JWTServiceImpl.TOKEN_ISSUER)) {
            return Mono.error(RegisterBasedUserNotFoundException::new);
        }
        log.info("Creating OAuth2 based user.");
        return mongoUserRepository.save(
                new User(
                        username,
                        email,
                        of(SIGNED),
                        of(id),
                        true
                )
        );
    }
}