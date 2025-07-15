package com.codeit.sb01otbooteam06.domain.profile.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

//@Profile("dev")
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String s3BucketName;
    private final String s3Region;

    @Override
    public String storeProfileImage(MultipartFile imageFile, UUID userId) {
        if (imageFile == null || imageFile.isEmpty()) return null;

        try {
            String extension = getExtension(imageFile.getOriginalFilename());
            String key = "profiles/" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(key)
                    .contentType(imageFile.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageFile.getBytes()));

            return "https://" + s3BucketName + ".s3." + s3Region + ".amazonaws.com/" + key;

        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : "";
    }
}

