package com.botoni.vsr.database.converter;

import com.botoni.vsr.database.enums.DevicePlatform;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DevicePlatformConverter implements AttributeConverter<DevicePlatform, String> {

    @Override
    public String convertToDatabaseColumn(DevicePlatform platform) {
        if (platform == null) {
            return null;
        }
        return platform.getValue();
    }

    @Override
    public DevicePlatform convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return DevicePlatform.fromValue(value);
    }
}
