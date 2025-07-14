package com.codeit.sb01otbooteam06.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @Email
    @NotBlank
    private String email;
}
