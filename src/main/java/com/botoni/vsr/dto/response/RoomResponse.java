package com.botoni.vsr.dto.response;

import java.util.List;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String name,
        UUID type,
        String notes,
        UUID origin,
        List<ItemResponse> items
) {
}
