package com.codeit.sb01otbooteam06.domain.profile.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.profile-image-dir}")
    private String profileImageDir;

    @Value("${file.profile-image-url-base}")
    private String profileImageUrlBase;

    /**
     * 프로필 이미지 저장 및 URL 반환
     */
    public String storeProfileImage(MultipartFile imageFile, UUID userId) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            String extension = getExtension(imageFile.getOriginalFilename());
            String fileName = userId + "." + extension;

            // 실제 로컬 저장 경로 생성
            File dir = new File(profileImageDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destination = new File(dir, fileName);
            imageFile.transferTo(destination);

            // 외부에 노출되는 URL 반환
            return profileImageUrlBase + fileName;
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
    }

    /**
     * 확장자 추출
     */
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : "";
    }
}
