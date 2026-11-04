package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.MembershipRole;
import com.botoni.vsr.lib.Texts;
import com.botoni.vsr.vo.Cnpj;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Nova empresa na plataforma. Quem cadastra entra como membro com o papel informado, que precisa ser de administrador
 * (sócio, representante ou MEI).
 */
public record CompanyAccountRequest(
        @NotBlank(message = "A razão social é obrigatória.")
        @Size(max = 200, message = "A razão social deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A razão social contém caracteres inválidos.")
        String name,

        @NotBlank(message = "O nome fantasia é obrigatório.")
        @Size(max = 200, message = "O nome fantasia deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome fantasia contém caracteres inválidos.")
        String tradeName,

        @NotNull(message = "O CNPJ é obrigatório.")
        Cnpj cnpj,

        @NotNull(message = "O seu papel na empresa é obrigatório.")
        MembershipRole role
) {

    public CompanyAccountRequest {
        name = Texts.clean(name);
        tradeName = Texts.clean(tradeName);
    }
}
