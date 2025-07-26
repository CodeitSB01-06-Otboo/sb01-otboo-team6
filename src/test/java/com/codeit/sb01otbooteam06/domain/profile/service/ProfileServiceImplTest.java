package com.codeit.sb01otbooteam06.domain.profile.service;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileUpdateRequest;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.profile.exception.ProfileNotFoundException;
import com.codeit.sb01otbooteam06.domain.profile.mapper.ProfileMapper;
import com.codeit.sb01otbooteam06.domain.profile.repository.ProfileRepository;
import com.codeit.sb01otbooteam06.domain.profile.util.GeoUtils;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.service.KakaoLocalClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    @DisplayName("프로필 이미지 URL을 정상적으로 조회할 수 있다")
    void getProfileImageUrl_success() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);
        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getProfileImageUrl()).willReturn("https://cdn.test.com/profile.jpg");

        String result = profileService.getProfileImageUrl(userId);

        assertThat(result).isEqualTo("https://cdn.test.com/profile.jpg");
    }

    @Test
    @DisplayName("프로필이 없을 경우 이미지 조회 시 예외 발생")
    void getProfileImageUrl_notFound() {
        UUID userId = UUID.randomUUID();
        given(profileRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfileImageUrl(userId));
    }

    @Test
    @DisplayName("기본 프로필 생성 성공")
    void createDefaultProfile_success() {
        UUID userId = UUID.randomUUID();
        String name = "기본이름";
        User user = mock(User.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        profileService.createDefaultProfile(userId, name);

        then(profileRepository).should().save(any(Profile.class));
    }

    @Test
    @DisplayName("프로필 수정: 일부만 주어졌을 때 기존 값 유지")
    void updateProfile_partialUpdate() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);

        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getGender()).willReturn(Gender.OTHER);
        given(profile.getTemperatureSensitivity()).willReturn(3);
        given(profile.getLocationNames()).willReturn(List.of("서울특별시", "강남구"));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        // 아무 필드도 입력하지 않음 (null)

        profileService.updateProfile(userId, request, null);

        then(profile).should().update(
                any(), // name
                eq(Gender.OTHER),
                any(), any(), any(), any(), any(),
                eq(List.of("서울특별시", "강남구")),
                eq(3),
                any()
        );
    }

    @Test
    @DisplayName("프로필 수정 시 location이 null이면 좌표 계산/위치명 조회를 수행하지 않는다")
    void updateProfile_locationNull_doesNotCallGeo() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);

        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getGender()).willReturn(Gender.OTHER);
        given(profile.getTemperatureSensitivity()).willReturn(3);
        given(profile.getLocationNames()).willReturn(List.of("기존", "위치"));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        // 모든 필드는 null로 두되 location 도 null

        profileService.updateProfile(userId, request, null);

        then(kakaoLocalClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("프로필 수정 시 profileImageUrl이 주어지면 setProfileImageUrl이 호출된다")
    void updateProfile_withImageUrl_setsImage() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getGender()).willReturn(Gender.OTHER);
        given(profile.getTemperatureSensitivity()).willReturn(3);
        given(profile.getLocationNames()).willReturn(List.of());

        String imageUrl = "https://cdn.test.com/image.jpg";

        profileService.updateProfile(userId, request, imageUrl);

        then(profile).should().setProfileImageUrl(imageUrl);
    }

    @Test
    @DisplayName("성별, 생년월일, 민감도가 null일 경우 기존 값 유지")
    void updateProfile_genderBirthTemp_nullKeepsOriginal() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);

        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getGender()).willReturn(Gender.OTHER);
        given(profile.getBirthDate()).willReturn(LocalDate.of(1990, 1, 1));
        given(profile.getTemperatureSensitivity()).willReturn(2);
        given(profile.getLocationNames()).willReturn(List.of());

        ProfileUpdateRequest request = new ProfileUpdateRequest(); // 전부 null

        profileService.updateProfile(userId, request, null);

        then(profile).should().update(
                any(), // name
                eq(Gender.OTHER),
                eq(LocalDate.of(1990, 1, 1)),
                isNull(), isNull(), isNull(), isNull(), any(),
                eq(2),
                any()
        );
    }

    @Test
    @DisplayName("프로필 수정: 모든 필드가 주어졌을 때 정상적으로 업데이트된다")
    void updateProfile_allFieldsPresent_success() {
        UUID userId = UUID.randomUUID();
        Profile profile = mock(Profile.class);

        given(profileRepository.findById(userId)).willReturn(Optional.of(profile));
        given(profile.getLocationNames()).willReturn(List.of("이전", "위치"));
        given(profile.getGender()).willReturn(Gender.MALE);
        given(profile.getTemperatureSensitivity()).willReturn(2);
        given(profile.getBirthDate()).willReturn(LocalDate.of(2000, 1, 1));

        // ✅ 요청 DTO 구성
        ProfileUpdateRequest.Location location = new ProfileUpdateRequest.Location();
        ReflectionTestUtils.setField(location, "latitude", 37.5);
        ReflectionTestUtils.setField(location, "longitude", 127.0);
        ReflectionTestUtils.setField(location, "locationNames", List.of("서울특별시", "강남구"));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "gender", "FEMALE");
        ReflectionTestUtils.setField(request, "birthDate", LocalDate.of(1995, 5, 5));
        ReflectionTestUtils.setField(request, "location", location);
        ReflectionTestUtils.setField(request, "temperatureSensitivity", 5);

        given(kakaoLocalClient.coordToRegion(37.5, 127.0)).willReturn(List.of("서울특별시", "강남구"));

        //  GeoUtils static mocking
        try (MockedStatic<GeoUtils> mockedGeoUtils = mockStatic(GeoUtils.class)) {
            mockedGeoUtils.when(() -> GeoUtils.convertToGrid(37.5, 127.0)).thenReturn(new int[]{60, 127});

            profileService.updateProfile(userId, request, "https://cdn.test.com/profile.jpg");

            then(profile).should().update(
                    eq("홍길동"),
                    eq(Gender.FEMALE),
                    eq(LocalDate.of(1995, 5, 5)),
                    eq(37.5), eq(127.0),
                    eq(60), eq(127),
                    eq(List.of("서울특별시", "강남구")),
                    eq(5),
                    eq("https://cdn.test.com/profile.jpg")
            );
        }
    }

}
