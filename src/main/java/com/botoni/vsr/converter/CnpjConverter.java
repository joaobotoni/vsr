package com.botoni.vsr.converter;

import com.botoni.vsr.vo.Cnpj;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CnpjConverter implements AttributeConverter<Cnpj, String> {

    @Override
    public String convertToDatabaseColumn(Cnpj cnpj) {
        if (isMissing(cnpj)) {
            return null;
        }
        return cnpj.value();
    }

    @Override
    public Cnpj convertToEntityAttribute(String value) {
        if (isMissing(value)) {
            return null;
        }
        return Cnpj.of(value);
    }

    private static boolean isMissing(Object value) {
        return value == null;
    }
}