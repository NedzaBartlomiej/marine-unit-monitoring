package pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.service.JWTService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.IpAuthProtectionVerificationToken;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class IpAuthProtectionServiceImpl extends AbstractVerificationTokenService implements IpAuthProtectionService {

    private static final Logger log = LoggerFactory.getLogger(IpAuthProtectionServiceImpl.class);
    private final String frontendUrl;
    private final long ipAddressTokenExpirationTime;
    private final UserService userService;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;
    private final String frontendUntrustedAuthenticationPath;
    private final JWTService jwtService;

    public IpAuthProtectionServiceImpl(EmailService emailService,
                                       MongoVerificationTokenRepository mongoVerificationTokenRepository,
                                       CustomVerificationTokenRepository customVerificationTokenRepository,
                                       UserService userService,
                                       @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
                                       @Value("${project-properties.expiration-times.verification.ip-address-token}") long ipAddressTokenExpirationTime,
                                       @Value("${project-properties.app.frontend-integration.endpoint-paths.untrusted-authentication}") String frontendUntrustedAuthenticationPath, JWTService jwtService) {
        super(emailService, mongoVerificationTokenRepository, customVerificationTokenRepository, userService);
        this.frontendUrl = frontendUrl;
        this.userService = userService;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.ipAddressTokenExpirationTime = ipAddressTokenExpirationTime;
        this.frontendUntrustedAuthenticationPath = frontendUntrustedAuthenticationPath;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> issue(String uid, Object ipAddress) {
        return userService.getUser(uid)
                .flatMap(user -> super.processIssue(
                        user,
                        new IpAuthProtectionVerificationToken(
                                user.getId(),
                                this.ipAddressTokenExpirationTime,
                                VerificationTokenType.TRUSTED_IP_ADDRESS_VERIFICATION.name(),
                                ipAddress
                        ),
                        "Marine Unit Monitoring - New untrusted account authentication detected."
                ));
    }

    @Override
    public Mono<VerificationToken> verify(String token) {
        log.info("Verifying IP address verification token.");
        return super.validateVerificationToken(mongoVerificationTokenRepository.findById(token))
                .flatMap(verificationToken -> userService.isUserExists(verificationToken.getUid())
                        .then(Mono.just(verificationToken))
                );
    }

    @Override
    protected Mono<Void> sendVerificationToken(String target, String title, String token) {
        return super.sendVerificationEmail(target, title, token);
    }

    @Override
    protected String buildVerificationMessage(String verificationUrl) {
        return "We have detected untrusted authentication activity on your account, please check it: " + verificationUrl;
    }

    @Override
    protected String buildVerificationUrl(String token) {
        return frontendUrl + frontendUntrustedAuthenticationPath + "/" + token;
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<Void> trustIpAddress(VerificationToken verificationToken) {
        log.info("Adding new trusted ip address for user.");
        return userService.trustIpAddress(verificationToken.getUid(), (String) verificationToken.getCarrierData())
                .then(mongoVerificationTokenRepository.delete(verificationToken));
    }

    @Transactional(transactionManager = "reactiveTransactionManager")
    @Override
    public Mono<Void> blockAccount(VerificationToken verificationToken) {
        log.info("Blocking user account after untrusted activity confirmation.");
        return userService.blockUser(verificationToken.getUid())
                .then(jwtService.invalidateAll(verificationToken.getUid()))
                .then(mongoVerificationTokenRepository.delete(verificationToken));
    }

    @Override
    public Mono<Void> processProtection(String uid, String ipAddress) {
        log.info("Processing trusted IP authentication protection.");
        return userService.getUser(uid)
                .map(User::getTrustedIpAddresses)
                .flatMapMany(Flux::fromIterable)
                .filter(ip -> ip.equals(ipAddress))
                .hasElement(ipAddress)
                .flatMap(isTrusted -> {
                    if (isTrusted) {
                        log.info("Trusted IP, continues the flow.");
                        return Mono.empty();
                    }
                    log.info("Untrusted IP, performing untrusted IP action.");
                    return this.issue(uid, ipAddress);
                });
    }
}
