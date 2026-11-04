package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.CompanyException;
import com.botoni.vsr.exception.enums.problem.CompanyProblem;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * Papel do membro na empresa. Sócio, representante e MEI administram (incluem e removem membros); funcionário opera.
 */
@Getter
public enum MembershipRole {

    PARTNER("socio", true),
    REPRESENTATIVE("representante", true),
    SOLE_PROPRIETOR("mei", true),
    EMPLOYEE("funcionario", false);

    @JsonValue
    private final String value;
    private final boolean admin;

    MembershipRole(String value, boolean admin) {
        this.value = value;
        this.admin = admin;
    }

    public static MembershipRole from(String value) {
        for (MembershipRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new CompanyException(CompanyProblem.UNKNOWN_ROLE, value);
    }
}
