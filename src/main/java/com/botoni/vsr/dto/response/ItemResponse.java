package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.ItemCondition;

import java.util.UUID;

public record ItemResponse(
        UUID id,
        String name,
        UUID type,
        String description,
        ItemCondition condition,
        String material,
        String notes,
        UUID origin
) {
}
