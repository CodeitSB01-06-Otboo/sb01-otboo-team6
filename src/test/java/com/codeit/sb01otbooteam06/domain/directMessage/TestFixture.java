package com.codeit.sb01otbooteam06.domain.directMessage;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestFixture {

    public static User newUser(String name) {
        User user = User.builder()
            .email(name + "@ex.com")
            .password("pw")
            .name(name)
            .role(Role.USER)
            .build();

        Profile profile = new Profile(
            user,                      // ★ 반드시 user 넘겨 줌
            name,
            Gender.MALE,
            LocalDate.of(1990,1,1),
            37.0, 127.0,
            60, 120,
            List.of("서울"),
            0,
            null
        );
        user.setProfile(profile);      // 양방향 연결

        return user;                   // ID 는 null → persist 시 자동 생성
    }

    private TestFixture() {}
}
