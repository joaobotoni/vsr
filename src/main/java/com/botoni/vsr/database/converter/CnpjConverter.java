package com.botoni.vsr.database.converter;

import com.botoni.vsr.vo.Cnpj;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CnpjConverter implements AttributeConverter<Cnpj, String> {

    @Override
    public String convertToDatabaseColumn(Cnpj cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.value();
    }

    @Override
    public Cnpj convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Cnpj.of(value);
    }
}
