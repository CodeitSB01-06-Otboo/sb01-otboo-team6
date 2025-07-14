package com.codeit.sb01otbooteam06.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthorDto {

    private UUID userId;
    private String name;
    private String profileImageUrl;
}