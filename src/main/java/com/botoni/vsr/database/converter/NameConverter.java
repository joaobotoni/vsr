package com.botoni.vsr.database.converter;

import com.botoni.vsr.vo.Name;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NameConverter implements AttributeConverter<Name, String> {

    @Override
    public String convertToDatabaseColumn(Name name) {
        if (name == null) {
            return null;
        }
        return name.value();
    }

    @Override
    public Name convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Name.of(value);
    }
}
