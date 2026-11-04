package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.PropertyCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PropertyCategoryConverter implements AttributeConverter<PropertyCategory, String> {

    @Override
    public String convertToDatabaseColumn(PropertyCategory category) {
        if (category == null) {
            return null;
        }
        return category.getValue();
    }

    @Override
    public PropertyCategory convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return PropertyCategory.from(value);
    }
}
