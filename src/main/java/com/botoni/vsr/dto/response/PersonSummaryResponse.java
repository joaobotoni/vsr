package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.PersonType;

import java.util.UUID;

public record PersonSummaryResponse(
        UUID id,
        PersonType type,
        String name,
        String document
) {
}
