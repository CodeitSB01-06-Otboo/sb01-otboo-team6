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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final KakaoLocalClient kakaoLocalClient;

    /**
     * 프로필 조회
     */
    @Override
    public ProfileDto getProfile(UUID userId) {
        Profile profile = findById(userId);
        return profileMapper.toDto(profile);
    }

    /**
     * 프로필 수정
     *
     * @param userId          수정할 사용자 ID
     * @param request         클라이언트로부터 받은 수정 요청 정보
     * @param profileImageUrl 저장된 이미지의 URL (null 이면 기존 URL 유지)
     */
    @Override
    @Transactional
    public void updateProfile(UUID userId, ProfileUpdateRequest request, String profileImageUrl) {

        Profile profile = findById(userId);

        double latitude = request.getLocation().getLatitude();
        double longitude = request.getLocation().getLongitude();
        int[] xy = GeoUtils.convertToGrid(latitude, longitude);
        int x = xy[0];
        int y = xy[1];

        List<String> locationNames = profile.getLocationNames();
        if (request.getLocation() != null) {
            locationNames = kakaoLocalClient.coordToRegion(latitude, longitude);
        }

        String genderStr = request.getGender();
        Gender gender = genderStr != null
                ? Gender.from(genderStr)
                : profile.getGender();

        Integer tempSens = request.getTemperatureSensitivity();
        int temperatureSensitivity = tempSens != null
                ? tempSens
                : profile.getTemperatureSensitivity();

        if (profileImageUrl != null) {
            profile.setProfileImageUrl(profileImageUrl);
        }

        profile.update(
                // 이름
                request.getName() != null ? request.getName() : profile.getName(),
                // 성별
                gender,
                // 생년월일
                request.getBirthDate() != null ? request.getBirthDate() : profile.getBirthDate(),
                // 위치
                latitude,
                longitude,
                x,
                y,
                locationNames,
                // 온도 민감도
                temperatureSensitivity,
                // 프로필 이미지 URL
                profileImageUrl != null ? profileImageUrl : profile.getProfileImageUrl()
        );
    }

    /**
     * (내부용) 프로필 엔티티의 이미지 URL 조회
     */
    @Override
    public String getProfileImageUrl(UUID userId) {
        Profile profile = findById(userId);
        return profile.getProfileImageUrl();
    }

    /**
     * 회원가입 시 호출: 기본 프로필 생성
     */
    @Override
    @Transactional
    public void createDefaultProfile(UUID userId, String name) {
        // ① 유저 엔티티 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));


        Profile profile = new Profile(
                user,
                name,
                Gender.OTHER,
                null,
                0.0, 0.0,
                0, 0,
                List.of(),
                3,
                null
        );

        // ③ 저장
        profileRepository.save(profile);
    }

    /** 공통: 프로필 조회 및 예외 처리 */
    private Profile findById(UUID userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}