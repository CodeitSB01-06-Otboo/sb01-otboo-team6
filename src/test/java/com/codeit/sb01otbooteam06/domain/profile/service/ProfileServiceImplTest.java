package com.codeit.sb01otbooteam06.domain.profile.service;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.profile.exception.ProfileNotFoundException;
import com.codeit.sb01otbooteam06.domain.profile.mapper.ProfileMapper;
import com.codeit.sb01otbooteam06.domain.profile.repository.ProfileRepository;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.service.KakaoLocalClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @InjectMocks
    private ProfileServiceImpl profileService;

    public ProfileServiceImplTest() {
        openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 프로필을 조회할 수 있다")
    void getProfile_success() {
        // given
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);

        ProfileDto.Location location = ProfileDto.Location.builder()
                .latitude(37.123)
                .longitude(127.456)
                .x(60)
                .y(127)
                .locationNames(List.of("서울특별시", "강남구"))
                .build();

        ProfileDto profileDto = ProfileDto.builder()
                .userId(userId)
                .name("홍길동")
                .gender(Gender.OTHER)
                .birthDate(LocalDate.of(1990, 1, 1))
                .location(location)
                .temperatureSensitivity(3)
                .profileImageUrl("https://cdn.test.com/image.jpg")
                .build();

        given(profileRepository.findWithLocationsById(userId)).willReturn(Optional.of(profile));
        given(profileMapper.toDto(profile)).willReturn(profileDto);


        ProfileDto result = profileService.getProfile(userId);


        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.gender()).isEqualTo(Gender.OTHER);
        assertThat(result.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.location().x()).isEqualTo(60);
        assertThat(result.temperatureSensitivity()).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 조회하면 예외 발생")
    void getProfile_notFound() {

        UUID userId = UUID.randomUUID();
        given(profileRepository.findWithLocationsById(userId)).willReturn(Optional.empty());


        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfile(userId));
    }
}
