package pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverification.common.service.VerificationTokenService;
import pl.bartlomiej.marineunitmonitoring.security.tokenverification.ipauthprotection.IpAuthProtectionVerificationToken;
import reactor.core.publisher.Mono;

public interface IpAuthProtectionService extends VerificationTokenService<IpAuthProtectionVerificationToken, String, Void> {

    Mono<Void> trustIpAddress(IpAuthProtectionVerificationToken verificationToken);

    Mono<Void> blockAccount(IpAuthProtectionVerificationToken verificationToken);

    Mono<Void> processProtection(String uid, String ipAddress);
}
