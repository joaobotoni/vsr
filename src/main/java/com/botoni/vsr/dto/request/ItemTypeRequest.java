package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ItemTypeRequest(
        @NotBlank(message = "O nome do tipo de item é obrigatório.")
        @Size(max = 100, message = "O nome do tipo de item deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome do tipo de item contém caracteres inválidos.")
        String name
) {

    public ItemTypeRequest {
        name = Texts.clean(name);
    }
}
