package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.MembershipRole;
import com.botoni.vsr.vo.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Inclusão de membro pelo e-mail da conta dele. A resposta é a mesma exista a conta ou não.
 */
public record MemberRequest(
        @NotNull(message = "O e-mail do membro é obrigatório.")
        Email email,

        @NotNull(message = "O papel do membro é obrigatório.")
        MembershipRole role
) {
}
