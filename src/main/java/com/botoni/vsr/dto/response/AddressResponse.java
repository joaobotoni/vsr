package com.botoni.vsr.dto.response;

import com.botoni.vsr.vo.Cep;

public record AddressResponse(
        Cep cep,
        String state,
        String city,
        String district,
        String street,
        String number,
        String complement
) {
}
