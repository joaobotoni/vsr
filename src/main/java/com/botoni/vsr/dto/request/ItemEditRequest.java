package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.ItemCondition;
import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ItemEditRequest(
        @Size(max = 100, message = "O nome do item deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome do item contém caracteres inválidos.")
        String name,

        @Size(max = 500, message = "A descrição do item deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A descrição do item contém caracteres inválidos.")
        String description,

        ItemCondition condition,

        @Size(max = 100, message = "O material do item deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O material do item contém caracteres inválidos.")
        String material,

        @Size(max = 2000, message = "As observações devem conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.NOTE, message = "As observações contêm caracteres inválidos.")
        String notes
) {

    public ItemEditRequest {
        name = Texts.clean(name);
        description = Texts.clean(description);
        material = Texts.clean(material);
        notes = Texts.note(notes);
    }
}
