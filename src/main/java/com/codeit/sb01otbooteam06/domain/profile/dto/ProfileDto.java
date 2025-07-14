package com.codeit.sb01otbooteam06.domain.profile.dto;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 프로필 응답 DTO (Swagger 명세 기반)
 */
@Builder
public record ProfileDto(
        UUID userId,                   // 사용자 ID
        String name,                   // 이름
        Gender gender,                 // 성별
        LocalDate birthDate,           // 생년월일
        Location location,             // 위치 정보
        int temperatureSensitivity,    // 온도 민감도
        String profileImageUrl         // 프로필 이미지 URL
) {

    /**
     * 위치 정보 DTO (Swagger 명세 기반)
     */
    @Builder
    public record Location(
            double latitude,               // 위도
            double longitude,              // 경도
            int x,                         // 기상청용 X 좌표
            int y,                         // 기상청용 Y 좌표
            List<String> locationNames     // 행정구역 이름 리스트
    ) {
    }
}
