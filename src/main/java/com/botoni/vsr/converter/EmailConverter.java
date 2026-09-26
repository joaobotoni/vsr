package com.botoni.vsr.converter;

import com.botoni.vsr.vo.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        if (isMissing(email)) {
            return null;
        }
        return email.value();
    }

    @Override
    public Email convertToEntityAttribute(String value) {
        if (isMissing(value)) {
            return null;
        }
        return Email.of(value);
    }

    private static boolean isMissing(Object value) {
        return value == null;
    }
}