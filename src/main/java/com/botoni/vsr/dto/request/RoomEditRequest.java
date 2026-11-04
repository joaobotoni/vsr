package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoomEditRequest(
        @Size(max = 100, message = "O nome do ambiente deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome do ambiente contém caracteres inválidos.")
        String name,

        @Size(max = 2000, message = "As observações devem conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.NOTE, message = "As observações contêm caracteres inválidos.")
        String notes
) {

    public RoomEditRequest {
        name = Texts.clean(name);
        notes = Texts.note(notes);
    }
}
