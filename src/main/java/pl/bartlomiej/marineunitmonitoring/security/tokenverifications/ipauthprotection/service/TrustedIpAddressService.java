package pl.bartlomiej.marineunitmonitoring.security.tokenverifications.ipauthprotection.service;

import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.VerificationToken;
import pl.bartlomiej.marineunitmonitoring.security.tokenverifications.common.service.VerificationTokenService;
import reactor.core.publisher.Mono;

public interface TrustedIpAddressService extends VerificationTokenService {

    Mono<Void> trustIpAddress(VerificationToken verificationToken);

    Mono<Void> blockAccount(VerificationToken verificationToken);
}
