package com.botoni.vsr.dto.response;

import com.botoni.vsr.database.enums.PersonType;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Phone;

import java.util.UUID;

/**
 * {@code document} é o CPF da pessoa física ou o CNPJ da pessoa jurídica; {@code tradeName} só existe na jurídica.
 */
public record PersonResponse(
        UUID id,
        PersonType type,
        String name,
        String tradeName,
        String document,
        Phone phone,
        Email email
) {
}
