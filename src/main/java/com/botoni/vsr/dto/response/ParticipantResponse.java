package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.ParticipantRole;

public record ParticipantResponse(
        PersonSummaryResponse person,
        ParticipantRole role
) {
}
