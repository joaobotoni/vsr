package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.InspectionType;
import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Início de vistoria. Na saída, {@code entry} aponta a vistoria de entrada finalizada do mesmo imóvel, e os ambientes
 * e itens dela são copiados.
 */
public record InspectionRequest(
        @NotNull(message = "O imóvel é obrigatório.")
        UUID property,

        @NotNull(message = "O tipo da vistoria é obrigatório.")
        InspectionType type,

        UUID entry,

        @Size(max = 2000, message = "As observações devem conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.NOTE, message = "As observações contêm caracteres inválidos.")
        String notes
) {

    public InspectionRequest {
        notes = Texts.note(notes);
    }
}
