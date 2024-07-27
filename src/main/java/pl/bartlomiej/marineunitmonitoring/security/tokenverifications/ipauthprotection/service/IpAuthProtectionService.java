package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.service.VerificationTokenService;
import reactor.core.publisher.Mono;

public interface IpAuthProtectionService extends VerificationTokenService {

    Mono<Void> trustIpAddress(VerificationToken verificationToken);

    Mono<Void> blockAccount(VerificationToken verificationToken);

    Mono<Void> processProtection(String uid, String ipAddress);
}
