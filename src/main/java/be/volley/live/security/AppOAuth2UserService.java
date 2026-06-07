package be.volley.live.security;

import be.volley.live.model.AppUser;
import be.volley.live.repository.AppUserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AppOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    public AppOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_email"), "No email returned by Google");
        }

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("user_not_found"),
                        "No access for " + email + ". Contact your administrator."));

        return new AppOAuth2User(appUser, oAuth2User.getAttributes());
    }
}
