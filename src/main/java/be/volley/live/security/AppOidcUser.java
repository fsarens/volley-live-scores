package be.volley.live.security;

import be.volley.live.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wraps AppUser + Google OIDC data into a Spring Security principal.
 */
public class AppOidcUser implements OidcUser {

    private final AppUser appUser;
    private final OidcUser delegate;

    public AppOidcUser(AppUser appUser, OidcUser delegate) {
        this.appUser = appUser;
        this.delegate = delegate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
    }

    @Override
    public String getName() { return appUser.getEmail(); }

    public String getTenantId() { return appUser.getTenantId(); }

    public AppUser getAppUser() { return appUser; }

    // Delegate OIDC methods
    @Override public Map<String, Object> getAttributes() { return delegate.getAttributes(); }
    @Override public Map<String, Object> getClaims() { return delegate.getClaims(); }
    @Override public OidcUserInfo getUserInfo() { return delegate.getUserInfo(); }
    @Override public OidcIdToken getIdToken() { return delegate.getIdToken(); }
}
