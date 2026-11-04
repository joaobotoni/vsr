package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.InspectionStatus;
import com.botoni.vsr.database.enums.InspectionType;

import java.time.Instant;
import java.util.UUID;

public record InspectionResponse(
        UUID id,
        UUID property,
        InspectionType type,
        InspectionStatus status,
        UUID entry,
        String notes,
        Instant startedAt,
        Instant finishedAt
) {
}
