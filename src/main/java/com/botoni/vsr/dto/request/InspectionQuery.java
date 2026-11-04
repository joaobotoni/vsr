package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.InspectionStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Filtros da listagem de vistorias ({@code ?property=&status=}); ausentes, não filtram.
 */
public record InspectionQuery(
        UUID property,
        InspectionStatus status
) {

    /** O status como gravado no banco, ou {@code null} sem filtro. */
    public String state() {
        return Optional.ofNullable(status).map(InspectionStatus::getValue).orElse(null);
    }
}
