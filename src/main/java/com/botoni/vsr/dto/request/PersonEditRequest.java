package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Phone;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alteração parcial da pessoa: campo ausente fica como está. Na pessoa física, o nome segue as regras do nome de
 * pessoa; {@code tradeName} só vale para pessoa jurídica. O documento não muda.
 */
public record PersonEditRequest(
        @Size(max = 200, message = "O nome deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome contém caracteres inválidos.")
        String name,

        @Size(max = 200, message = "O nome fantasia deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome fantasia contém caracteres inválidos.")
        String tradeName,

        Phone phone,

        Email email
) {

    public PersonEditRequest {
        name = Texts.clean(name);
        tradeName = Texts.clean(tradeName);
    }
}
