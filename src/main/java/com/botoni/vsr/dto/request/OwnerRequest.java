package com.botoni.vsr.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OwnerRequest(
        @NotNull(message = "A pessoa é obrigatória.")
        UUID person
) {
}
