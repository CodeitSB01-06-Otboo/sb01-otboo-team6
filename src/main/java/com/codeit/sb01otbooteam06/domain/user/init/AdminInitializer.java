package com.codeit.sb01otbooteam06.domain.user.init;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.profile.repository.ProfileRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional  // ⭐️ 트랜잭션 보장
    public void run(String... args) {
        String adminEmail = "admin@example.com";

        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("admin1234"))
                .name("관리자")
                .role(Role.ADMIN)
                .locked(false)
                .linkedOAuthProviders(null)
                .build();

        admin = userRepository.saveAndFlush(admin);  // ⭐️ 영속화 + flush

        Profile profile = Profile.builder()
                .user(admin)
                .name("관리자")
                .gender(Gender.OTHER)
                .birthDate(LocalDate.of(1990, 1, 1))
                .latitude(null)
                .longitude(null)
                .x(null)
                .y(null)
                .locationNames(List.of("서울특별시 강남구"))
                .temperatureSensitivity(3)
                .profileImageUrl(null)
                .build();

        profileRepository.save(profile);
    }
}

