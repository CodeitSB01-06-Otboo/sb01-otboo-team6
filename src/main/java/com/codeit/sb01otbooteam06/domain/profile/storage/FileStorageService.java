package com.codeit.sb01otbooteam06.domain.profile.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    String storeProfileImage(MultipartFile imageFile, UUID userId);
}
