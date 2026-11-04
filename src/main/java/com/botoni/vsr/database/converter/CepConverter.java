package com.botoni.vsr.database.converter;

import com.botoni.vsr.vo.Cep;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CepConverter implements AttributeConverter<Cep, String> {

    @Override
    public String convertToDatabaseColumn(Cep cep) {
        if (cep == null) {
            return null;
        }
        return cep.value();
    }

    @Override
    public Cep convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Cep.of(value);
    }
}
