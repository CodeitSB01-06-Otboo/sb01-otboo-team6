package com.codeit.sb01otbooteam06.global.s3;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.s3.exception.S3UploadException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private static final String FILE_NAME_DELIMITER = "_";
  private static final String PATH_DELIMITER = "/";
  private static final String S3_PROTOCOL = "https";
  private static final String S3_HOST_TEMPLATE = "%s.s3.%s.amazonaws.com";

  private final S3Client s3Client;
  private final String s3BucketName;
  private final String s3Region;

  public String upload(MultipartFile file, String directory) {
    String s3ObjectKey = generateS3ObjectKey(file, directory);

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(s3BucketName)
          .key(s3ObjectKey)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
    } catch (IOException | SdkException exception) {
      throw new S3UploadException(ErrorCode.S3_UPLOAD_ERROR, exception);
    }

    return resolveS3ObjectUrl(s3ObjectKey);
  }

  private String generateS3ObjectKey(MultipartFile file, String directory) {
    String originalFileName = file.getOriginalFilename();
    if (originalFileName == null || originalFileName.isBlank()) {
      throw new S3UploadException(ErrorCode.FILE_NAME_MISSING);
    }
    String uniqueFileName = UUID.randomUUID() + FILE_NAME_DELIMITER + originalFileName;
    return directory + PATH_DELIMITER + uniqueFileName;
  }

  private String resolveS3ObjectUrl(String s3ObjectKey) {
    String s3Host = String.format(S3_HOST_TEMPLATE, s3BucketName, s3Region);
    return UriComponentsBuilder.newInstance()
        .scheme(S3_PROTOCOL)
        .host(s3Host)
        .path(s3ObjectKey)
        .build()
        .toUriString();
  }

}
