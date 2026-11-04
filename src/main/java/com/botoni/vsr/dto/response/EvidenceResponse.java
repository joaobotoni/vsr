package com.botoni.vsr.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EvidenceResponse(
        UUID id,
        UUID item,
        String contentType,
        Integer size,
        String description,
        Instant receivedAt
) {
}
