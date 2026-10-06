package com.botoni.vsr.database.converter;

import com.botoni.vsr.vo.PasswordHash;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PasswordHashConverter implements AttributeConverter<PasswordHash, String> {

    @Override
    public String convertToDatabaseColumn(PasswordHash passwordHash) {
        if (passwordHash == null) {
            return null;
        }
        return passwordHash.value();
    }

    @Override
    public PasswordHash convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return PasswordHash.of(value);
    }
}
