package com.codeit.sb01otbooteam06.domain.auth.oauth;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        final String provider = "google";
        final String providerId = (String) attributes.get("sub");

        String rawEmail = (String) attributes.get("email");
        final String email = (rawEmail == null || rawEmail.isBlank())
                ? provider + "_" + providerId + "@noemail.com"
                : rawEmail;

        final String name = (String) attributes.getOrDefault("name", "GoogleUser_" + UUID.randomUUID().toString().substring(0, 5));

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.createSocialUser(provider, providerId, email, name);
                    Profile profile = new Profile(
                            newUser,
                            name,
                            Gender.OTHER,
                            LocalDate.of(2000, 1, 1),
                            null, null, null, null,
                            List.of(),
                            3,
                            null
                    );
                    newUser.setProfile(profile);
                    return userRepository.save(newUser);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "sub"
        );
    }
}
