package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.bartlomiej.marineunitmonitoring.emailsending.EmailService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationTokenType;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.CustomVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.repository.MongoVerificationTokenRepository;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.service.AbstractVerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection.IpAddressVerificationToken;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;
import reactor.core.publisher.Mono;


@Service
public class TrustedIpAddressServiceImpl extends AbstractVerificationTokenService implements TrustedIpAddressService {

    private static final Logger log = LoggerFactory.getLogger(TrustedIpAddressServiceImpl.class);
    private final String frontendUrl;
    private final long ipAddressTokenExpirationTime;
    private final UserService userService;
    private final MongoVerificationTokenRepository mongoVerificationTokenRepository;

    public TrustedIpAddressServiceImpl(EmailService emailService,
                                       MongoVerificationTokenRepository mongoVerificationTokenRepository,
                                       CustomVerificationTokenRepository customVerificationTokenRepository,
                                       UserService userService,
                                       @Value("${project-properties.app.frontend-integration.base-url}") String frontendUrl,
                                       @Value("${project-properties.expiration-times.verification.ip-address-token}") long ipAddressTokenExpirationTime) {
        super(emailService, mongoVerificationTokenRepository, customVerificationTokenRepository, userService);
        this.frontendUrl = frontendUrl;
        this.userService = userService;
        this.mongoVerificationTokenRepository = mongoVerificationTokenRepository;
        this.ipAddressTokenExpirationTime = ipAddressTokenExpirationTime;
    }

    @Override
    public Mono<Void> issue(String uid, Object ipAddress) {
        log.info("ipAddress: {}", ipAddress);
        return userService.getUser(uid)
                .flatMap(user -> super.processIssue(
                        user,
                        new IpAddressVerificationToken(
                                user.getId(),
                                this.ipAddressTokenExpirationTime,
                                VerificationTokenType.TRUSTED_IP_ADDRESS_VERIFICATION.name(),
                                ipAddress
                        ),
                        "Marine Unit Monitoring - New untrusted account activity report."
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
    protected String buildVerificationMessage(String verificationUrl) {
        return "We have detected untrusted authentication activity on your account, please take a look at, and confirm it: " + verificationUrl;
    }

    @Override
    protected String buildVerificationUrl(String token) { // todo - implement an activity log and then return the untrusted-activity-log path
        return "IpAddressVerificationToken: " + token;
    }

    @Override
    public Mono<Void> trustIpAddress(VerificationToken verificationToken) {
        log.info("Adding new trusted ip address for user.");
        return userService.trustIpAddress(verificationToken.getUid(), (String) verificationToken.getCarrierData())
                .then(mongoVerificationTokenRepository.delete(verificationToken));
    }

    @Override
    public Mono<Void> blockAccount(VerificationToken verificationToken) {
        log.info("Blocking user account after untrusted activity confirmation.");
        return userService.blockUser(verificationToken.getUid())
                .then(mongoVerificationTokenRepository.delete(verificationToken));
    }
}
