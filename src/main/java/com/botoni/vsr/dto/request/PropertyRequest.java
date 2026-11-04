package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.PropertyCategory;
import com.botoni.vsr.database.enums.PropertyType;
import com.botoni.vsr.lib.Texts;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Novo imóvel. Com {@code company}, o imóvel é da empresa (o usuário precisa ser membro dela); sem, é do usuário.
 */
public record PropertyRequest(
        @Size(max = 200, message = "A descrição deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A descrição contém caracteres inválidos.")
        String description,

        @NotNull(message = "O tipo do imóvel é obrigatório.")
        PropertyType type,

        @NotNull(message = "A categoria do imóvel é obrigatória.")
        PropertyCategory category,

        @Valid
        @NotNull(message = "O endereço é obrigatório.")
        AddressRequest address,

        UUID company
) {

    public PropertyRequest {
        description = Texts.clean(description);
    }
}
