package pl.bartlomiej.marineunitmonitoring.security.grantedauthorities;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.UserService;

import java.util.Collection;

@Component
public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserService userService;

    public CustomJwtGrantedAuthoritiesConverter(UserService userService) {
        this.userService = userService;
    }


    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {

        User subjectUser = userService.createOrUpdateOAuth2BasedUser(
                source.getSubject(),
                source.getClaimAsString("name"),
                source.getClaimAsString("email")
        );

        return subjectUser.getRoles().stream()
                .<GrantedAuthority>map(role -> new UserRoleAuthority(role.name()))
                .toList();
    }
}