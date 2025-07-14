package com.codeit.sb01otbooteam06.domain.profile.service;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileUpdateRequest;

import java.util.UUID;

/**
 * 프로필 서비스 인터페이스 (Swagger 명세 기반)
 */
public interface ProfileService {

    /**
     * 프로필 단건 조회
     */
    ProfileDto getProfile(UUID userId);

    /**
     * 프로필 전체 수정
     */
    void updateProfile(UUID userId, ProfileUpdateRequest request, String profileImageUrl);

    /**
     * 프로필 이미지 URL 조회 (내부 로직용)
     */
    String getProfileImageUrl(UUID userId);

    /**
     * 기본 프로필 생성 (회원가입 시 호출)
     */
    void createDefaultProfile(UUID userId, String name);
}