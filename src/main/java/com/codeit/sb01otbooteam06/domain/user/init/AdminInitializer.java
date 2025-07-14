package com.codeit.sb01otbooteam06.domain.user.init;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 서버 시작 시 어드민 계정 자동 생성
 */
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@example.com";

        // 이미 어드민 계정이 존재하면 생성하지 않음
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("admin1234")) // 개발용 초기 비밀번호
                .name("관리자")
                .role(Role.ADMIN)
                .locked(false)
                .linkedOAuthProviders(null)
                .build();

        userRepository.save(admin);
    }
}
