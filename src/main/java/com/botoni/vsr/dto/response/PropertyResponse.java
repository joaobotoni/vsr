package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.PropertyCategory;
import com.botoni.vsr.database.enums.PropertyType;

import java.util.UUID;

public record PropertyResponse(
        UUID id,
        String description,
        PropertyType type,
        PropertyCategory category,
        AddressResponse address
) {
}
