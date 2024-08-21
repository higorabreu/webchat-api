package com.codemachine.webchat.dto;

import jakarta.validation.constraints.NotNull;

public record RequestLoginUser(
    @NotNull
    String username,
    @NotNull
    String password
    )
    {}
