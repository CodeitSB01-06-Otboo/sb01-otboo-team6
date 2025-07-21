package com.codeit.sb01otbooteam06.domain.profile.mapper;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

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
        if (profile == null) return null;

        return ProfileDto.builder()
                .userId(profile.getUser().getId())
                .name(profile.getName())
                .gender(profile.getGender())
                .birthDate(profile.getBirthDate())
                .location(ProfileDto.Location.builder()
                        .latitude(profile.getLatitude() != null ? profile.getLatitude() : 0.0)
                        .longitude(profile.getLongitude() != null ? profile.getLongitude() : 0.0)
                        .x(profile.getX() != null ? profile.getX() : 0)
                        .y(profile.getY() != null ? profile.getY() : 0)
                        .locationNames(profile.getLocationNames() != null ? profile.getLocationNames() : List.of())
                        .build())
                .temperatureSensitivity(profile.getTemperatureSensitivity())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }
}
