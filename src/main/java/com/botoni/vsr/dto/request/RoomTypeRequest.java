package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Tipo de ambiente personalizado, com os itens típicos dele (tipos de item padrão ou do próprio usuário).
 */
public record RoomTypeRequest(
        @NotBlank(message = "O nome do tipo de ambiente é obrigatório.")
        @Size(max = 100, message = "O nome do tipo de ambiente deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome do tipo de ambiente contém caracteres inválidos.")
        String name,

        @Size(max = 100, message = "O tipo de ambiente pode ter no máximo {max} itens.")
        List<UUID> items
) {

    public RoomTypeRequest {
        name = Texts.clean(name);
        items = items == null ? List.of() : List.copyOf(items);
    }
}
