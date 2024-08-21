package com.codemachine.webchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestRegisterUser(
    @NotBlank
    String username,
    @NotBlank
    String email,
    @NotBlank
    String name,
    @NotNull
    String password
    )
    {}
