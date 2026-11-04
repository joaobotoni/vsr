package com.botoni.vsr.dto.request;

import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import com.botoni.vsr.vo.Phone;
import jakarta.validation.constraints.NotNull;

/**
 * Pessoa física do cadastro do usuário (proprietário, inquilino). Telefone e e-mail são opcionais.
 */
public record IndividualRequest(
        @NotNull(message = "O nome é obrigatório.")
        Name name,

        @NotNull(message = "O CPF é obrigatório.")
        Cpf cpf,

        Phone phone,

        Email email
) {
}
