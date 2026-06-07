package be.volley.live.security;

import be.volley.live.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wraps AppUser + Google OAuth2 attributes into a Spring Security principal.
 */
public class AppOAuth2User implements OAuth2User {

    private final AppUser appUser;
    private final Map<String, Object> attributes;

    public AppOAuth2User(AppUser appUser, Map<String, Object> attributes) {
        this.appUser = appUser;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
    }

    @Override
    public String getName() { return appUser.getEmail(); }

    public String getTenantId() { return appUser.getTenantId(); }

    public AppUser getAppUser() { return appUser; }
}
