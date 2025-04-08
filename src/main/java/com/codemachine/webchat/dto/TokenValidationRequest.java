package com.codemachine.webchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenValidationRequest {
    @NotBlank(message = "Username is required")
    private String username;
}