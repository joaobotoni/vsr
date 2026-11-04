package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import com.botoni.vsr.vo.Cep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record AddressRequest(
        @NotNull(message = "O CEP é obrigatório.")
        Cep cep,

        @NotBlank(message = "O estado é obrigatório.")
        @Pattern(regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
                message = "O estado deve ser a sigla de uma unidade federativa.")
        String state,

        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = 100, message = "A cidade deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A cidade contém caracteres inválidos.")
        String city,

        @NotBlank(message = "O bairro é obrigatório.")
        @Size(max = 100, message = "O bairro deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O bairro contém caracteres inválidos.")
        String district,

        @NotBlank(message = "O logradouro é obrigatório.")
        @Size(max = 200, message = "O logradouro deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O logradouro contém caracteres inválidos.")
        String street,

        @NotBlank(message = "O número é obrigatório.")
        @Size(max = 20, message = "O número deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O número contém caracteres inválidos.")
        String number,

        @Size(max = 100, message = "O complemento deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O complemento contém caracteres inválidos.")
        String complement
) {

    public AddressRequest {
        state = upper(Texts.clean(state));
        city = Texts.clean(city);
        district = Texts.clean(district);
        street = Texts.clean(street);
        number = Texts.clean(number);
        complement = Texts.clean(complement);
    }

    private static String upper(String value) {
        if (value == null) {
            return null;
        }
        return value.toUpperCase(Locale.ROOT);
    }
}
