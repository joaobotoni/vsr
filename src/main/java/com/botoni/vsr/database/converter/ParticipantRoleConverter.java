package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.ParticipantRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ParticipantRoleConverter implements AttributeConverter<ParticipantRole, String> {

    @Override
    public String convertToDatabaseColumn(ParticipantRole role) {
        if (role == null) {
            return null;
        }
        return role.getValue();
    }

    @Override
    public ParticipantRole convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return ParticipantRole.from(value);
    }
}
