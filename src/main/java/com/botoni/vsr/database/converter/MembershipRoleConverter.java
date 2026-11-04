package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.MembershipRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MembershipRoleConverter implements AttributeConverter<MembershipRole, String> {

    @Override
    public String convertToDatabaseColumn(MembershipRole role) {
        if (role == null) {
            return null;
        }
        return role.getValue();
    }

    @Override
    public MembershipRole convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return MembershipRole.from(value);
    }
}
