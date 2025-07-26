package com.codeit.sb01otbooteam06.config;

import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtAuthenticationFilter;
import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.auth.oauth.CustomAuthorizationRequestResolver;
import com.codeit.sb01otbooteam06.domain.auth.oauth.CustomOAuth2UserService;
import com.codeit.sb01otbooteam06.domain.auth.oauth.DelegatingOAuth2UserService;
import com.codeit.sb01otbooteam06.domain.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final DelegatingOAuth2UserService delegatingOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserRepository userRepository;  //  추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationRequestResolver resolver =
                new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(resolver)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(delegatingOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

