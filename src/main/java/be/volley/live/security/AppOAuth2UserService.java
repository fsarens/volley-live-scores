package be.volley.live.security;

import be.volley.live.model.AppUser;
import be.volley.live.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AppOAuth2UserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(AppOAuth2UserService.class);

    private final AppUserRepository appUserRepository;

    public AppOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        log.info("OAuth2 login attempt — email from Google: '{}'", email);
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_email"), "No email returned by Google");
        }

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("No app_users record found for email: '{}'", email);
                    return new OAuth2AuthenticationException(
                            new OAuth2Error("user_not_found"),
                            "No access for " + email + ". Contact your administrator.");
                });

        log.info("OAuth2 login success — email: '{}', role: {}, tenantId: {}", email, appUser.getRole(), appUser.getTenantId());
        return new AppOidcUser(appUser, oidcUser);
    }
}
