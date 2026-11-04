package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.InspectionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InspectionStatusConverter implements AttributeConverter<InspectionStatus, String> {

    @Override
    public String convertToDatabaseColumn(InspectionStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public InspectionStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return InspectionStatus.from(value);
    }
}
