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
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        final String provider = "kakao";
        final String providerId = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String rawEmail = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String rawName = properties != null ? (String) properties.get("nickname") : null;

        final String name = (rawName != null && !rawName.isBlank())
                ? rawName
                : "KakaoUser_" + UUID.randomUUID().toString().substring(0, 5);

        final String email = (rawEmail != null && !rawEmail.isBlank())
                ? rawEmail
                : provider + "_" + providerId + "@noemail.com";

        //  로그 출력
        System.out.println("카카오 attributes = " + attributes);
        System.out.println("kakao_account = " + kakaoAccount);
        System.out.println("nickname = " + name);

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.createSocialUser(provider, providerId, email, name);
                    Profile profile = new Profile(newUser, name, Gender.OTHER, LocalDate.of(2000, 1, 1),
                            null, null, null, null, List.of(), 3, null);
                    newUser.setProfile(profile);
                    return userRepository.save(newUser);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "id"
        );
    }
}
