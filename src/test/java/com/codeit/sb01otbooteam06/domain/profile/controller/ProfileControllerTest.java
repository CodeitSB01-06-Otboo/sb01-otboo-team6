package com.codeit.sb01otbooteam06.domain.profile.controller;

import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto;
import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileDto.Location;
import com.codeit.sb01otbooteam06.domain.profile.dto.ProfileUpdateRequest;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.service.ProfileService;
import com.codeit.sb01otbooteam06.domain.profile.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(ProfileController.class)
@Import(ProfileControllerTest.TestConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("GET /api/users/{userId}/profiles - 프로필 조회")
    void getProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        ProfileDto profileDto = ProfileDto.builder()
                .userId(userId)
                .name("홍길동")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .location(Location.builder()
                        .latitude(37.5665)
                        .longitude(126.9780)
                        .x(60)
                        .y(127)
                        .locationNames(List.of("서울특별시", "중구"))
                        .build())
                .temperatureSensitivity(0)
                .profileImageUrl("https://example.com/image.jpg")
                .build();

        Mockito.when(profileService.getProfile(userId)).thenReturn(profileDto);

        mockMvc.perform(get("/api/users/{userId}/profiles", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.gender").value("MALE"));
    }

    @Test
    @DisplayName("PATCH /api/users/{userId}/profiles - 프로필 수정")
    void updateProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        String jsonRequest = """
            {
              "name": "김철수",
              "gender": "MALE",
              "birthDate": "1995-05-10",
              "locationNames": ["서울특별시", "강남구"],
              "latitude": 37.5,
              "longitude": 127.0,
              "temperatureSensitivity": 1
            }
        """;

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json", jsonRequest.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes()
        );

        Mockito.when(fileStorageService.storeProfileImage(any(), eq(userId)))
                .thenReturn("https://example.com/profile.jpg");

        Mockito.doNothing().when(profileService)
                .updateProfile(eq(userId), any(ProfileUpdateRequest.class), any(String.class));

        ProfileDto updatedDto = ProfileDto.builder()
                .userId(userId)
                .name("김철수")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 10))
                .location(Location.builder()
                        .latitude(37.5)
                        .longitude(127.0)
                        .x(62)
                        .y(128)
                        .locationNames(List.of("서울특별시", "강남구"))
                        .build())
                .temperatureSensitivity(1)
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        Mockito.when(profileService.getProfile(userId)).thenReturn(updatedDto);

        mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
                        .file(requestPart)
                        .file(imageFile)
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("김철수"))
                .andExpect(jsonPath("$.gender").value("MALE"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProfileService profileService() {
            return Mockito.mock(ProfileService.class);
        }

        @Bean
        public FileStorageService fileStorageService() {
            return Mockito.mock(FileStorageService.class);
        }

        @Bean
        public ProfileController profileController(ProfileService profileService, FileStorageService fileStorageService) {
            return new ProfileController(profileService, fileStorageService);
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

}
