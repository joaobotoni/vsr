package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.PropertyCategory;
import com.botoni.vsr.database.enums.PropertyType;
import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alteração parcial do imóvel: campo ausente (ou nulo) fica como está. O endereço não muda; outro endereço é outro imóvel.
 */
public record PropertyEditRequest(
        @Size(max = 200, message = "A descrição deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A descrição contém caracteres inválidos.")
        String description,

        PropertyType type,

        PropertyCategory category
) {

    public PropertyEditRequest {
        description = Texts.clean(description);
    }
}
