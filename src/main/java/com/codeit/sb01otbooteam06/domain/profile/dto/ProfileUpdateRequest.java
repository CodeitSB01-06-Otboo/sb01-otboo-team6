package com.codeit.sb01otbooteam06.domain.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 프로필 업데이트 요청 DTO (Swagger 명세 기반)
 */
@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank
    private String name;

    @NotNull
    private String gender;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Location location;

    @NotNull
    private Integer temperatureSensitivity;

    private MultipartFile image;

    @Getter
    @Setter
    public static class Location {

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        @NotNull
        private List<String> locationNames;
    }

}