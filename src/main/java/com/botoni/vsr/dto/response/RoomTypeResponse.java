package com.botoni.vsr.dto.response;

import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
        UUID id,
        String name,
        boolean standard,
        List<ItemTypeResponse> items
) {
}
