package com.codemachine.webchat.dto;

import java.util.UUID;

public record ResponseUser(
        UUID id,
        String username,
        String email,
        String name
) {}
