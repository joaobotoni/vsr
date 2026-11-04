package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.ItemCondition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ItemConditionConverter implements AttributeConverter<ItemCondition, String> {

    @Override
    public String convertToDatabaseColumn(ItemCondition condition) {
        if (condition == null) {
            return null;
        }
        return condition.getValue();
    }

    @Override
    public ItemCondition convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return ItemCondition.from(value);
    }
}
