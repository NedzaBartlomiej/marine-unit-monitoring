package pl.bartlomiej.marineunitmonitoring.security.authentication.grantedauthorities;

import org.springframework.security.core.GrantedAuthority;

public record UserRoleAuthority(String authority) implements GrantedAuthority {

    @Override
    public String getAuthority() {
        return "ROLE_" + authority;
    }
}
