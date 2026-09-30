package com.botoni.vsr.converter;

import com.botoni.vsr.vo.PasswordHash;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PasswordHashConverter implements AttributeConverter<PasswordHash, String> {

    @Override
    public String convertToDatabaseColumn(PasswordHash passwordHash) {
        if (isMissing(passwordHash)) {
            return null;
        }
        return passwordHash.value();
    }

    @Override
    public PasswordHash convertToEntityAttribute(String value) {
        if (isMissing(value)) {
            return null;
        }
        return PasswordHash.of(value);
    }

    private static boolean isMissing(Object value) {
        return value == null;
    }
}