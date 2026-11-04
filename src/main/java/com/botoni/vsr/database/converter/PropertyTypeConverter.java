package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.PropertyType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PropertyTypeConverter implements AttributeConverter<PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(PropertyType type) {
        if (type == null) {
            return null;
        }
        return type.getValue();
    }

    @Override
    public PropertyType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return PropertyType.from(value);
    }
}
