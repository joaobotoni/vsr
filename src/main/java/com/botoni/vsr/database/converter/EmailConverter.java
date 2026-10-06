package com.botoni.vsr.database.converter;

import com.botoni.vsr.vo.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        if (email == null) {
            return null;
        }
        return email.value();
    }

    @Override
    public Email convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Email.of(value);
    }
}
