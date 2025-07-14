package com.codeit.sb01otbooteam06.domain.profile.controller;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileUpdateRequest;
import com.codeit.sb01otbooteam06.domain.profile.service.ProfileService;
import com.codeit.sb01otbooteam06.domain.profile.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    /**
     * 프로필 조회
     */
    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID userId) {
        ProfileDto profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * 프로필 수정
     *
     * - consumes = multipart/form-data 로 설정하여
     *   JSON 요청(request) + 이미지 파일(image) 둘 다 받을 수 있도록 함
     * - @RequestPart("request") 는 JSON 바디를, @RequestPart("image") 는 파일을 바인딩
     */
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable UUID userId,
            @RequestPart("request") ProfileUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        // ② MultipartFile 을 FileStorageService 로 저장하고 URL 획득
        String imageUrl = fileStorageService.storeProfileImage(imageFile, userId);

        // ③ 서비스에 요청 DTO 와 imageUrl 을 전달하여 프로필 업데이트 실행
        profileService.updateProfile(userId, request, imageUrl);

        // ④ 업데이트된 프로필을 다시 조회하여 반환
        ProfileDto updatedProfile = profileService.getProfile(userId);
        return ResponseEntity.ok(updatedProfile);
    }
}