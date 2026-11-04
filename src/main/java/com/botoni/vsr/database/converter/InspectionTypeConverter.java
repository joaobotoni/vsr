package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.InspectionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InspectionTypeConverter implements AttributeConverter<InspectionType, String> {

    @Override
    public String convertToDatabaseColumn(InspectionType type) {
        if (type == null) {
            return null;
        }
        return type.getValue();
    }

    @Override
    public InspectionType convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return InspectionType.from(value);
    }
}
