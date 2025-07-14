package com.codeit.sb01otbooteam06.domain.profile.mapper;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import org.springframework.stereotype.Component;

/**
 * 프로필 엔티티 ↔ DTO 변환 매퍼 (Swagger 명세 기반)
 */
@Component
public class ProfileMapper {

    /**
     * 엔티티 → DTO 변환
     * @param profile 프로필 엔티티
     * @return 프로필 응답 DTO
     */
    public ProfileDto toDto(Profile profile) {
        return ProfileDto.builder()
                .userId(profile.getUser().getId())                   // User의 UUID 반환
                .name(profile.getName())                             // 이름
                .gender(profile.getGender())                         // Gender enum 그대로 반환
                .birthDate(profile.getBirthDate())                   // 생년월일
                .location(ProfileDto.Location.builder()
                        .latitude(profile.getLatitude())            // 위도
                        .longitude(profile.getLongitude())          // 경도
                        .x(profile.getX())                          // X 좌표 (기상청용)
                        .y(profile.getY())                          // Y 좌표 (기상청용)
                        .locationNames(profile.getLocationNames())  // 행정구역 이름 리스트
                        .build())
                .temperatureSensitivity(profile.getTemperatureSensitivity()) // 온도 민감도
                .profileImageUrl(profile.getProfileImageUrl())       // 프로필 이미지 URL
                .build();
    }
}
