package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.MembershipRole;
import com.botoni.vsr.vo.Email;

import java.util.UUID;

/**
 * Membro da empresa: {@code id} é o UUID público do usuário.
 */
public record MemberResponse(
        UUID id,
        String name,
        Email email,
        MembershipRole role
) {
}
