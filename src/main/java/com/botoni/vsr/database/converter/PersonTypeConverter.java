package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.PersonType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PersonTypeConverter implements AttributeConverter<PersonType, String> {

    @Override
    public String convertToDatabaseColumn(PersonType type) {
        if (type == null) {
            return null;
        }
        return type.getValue();
    }

    @Override
    public PersonType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return PersonType.from(value);
    }
}
