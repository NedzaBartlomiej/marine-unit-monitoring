package pl.bartlomiej.marineunitmonitoring.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pl.bartlomiej.marineunitmonitoring.user.User;
import pl.bartlomiej.marineunitmonitoring.user.service.sync.UserService;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Component
public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserService userService;

    public CustomJwtGrantedAuthoritiesConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        User subjectUser = userService.getUserByOpenId(source.getSubject());
        return subjectUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(toList()); // why this working, but .toList() not
    }
}
