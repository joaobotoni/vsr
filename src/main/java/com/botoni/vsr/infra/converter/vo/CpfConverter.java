package com.botoni.vsr.infra.converter.vo;

import com.botoni.vsr.shared.vo.Cpf;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CpfConverter implements AttributeConverter<Cpf, String> {

    @Override
    public String convertToDatabaseColumn(Cpf cpf) {
        if (isMissing(cpf)) {
            return null;
        }
        return cpf.value();
    }

    @Override
    public Cpf convertToEntityAttribute(String value) {
        if (isMissing(value)) {
            return null;
        }
        return Cpf.of(value);
    }

    private static boolean isMissing(Object value) {
        return value == null;
    }
}
