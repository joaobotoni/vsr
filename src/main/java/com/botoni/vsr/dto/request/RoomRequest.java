package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Novo ambiente. Com {@code type} (tipo de ambiente do catálogo), o ambiente já nasce com os itens típicos dele.
 */
public record RoomRequest(
        @NotBlank(message = "O nome do ambiente é obrigatório.")
        @Size(max = 100, message = "O nome do ambiente deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome do ambiente contém caracteres inválidos.")
        String name,

        UUID type,

        @Size(max = 2000, message = "As observações devem conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.NOTE, message = "As observações contêm caracteres inválidos.")
        String notes
) {

    public RoomRequest {
        name = Texts.clean(name);
        notes = Texts.note(notes);
    }
}
