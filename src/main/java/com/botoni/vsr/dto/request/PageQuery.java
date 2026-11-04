package com.botoni.vsr.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Paginação das listagens ({@code ?page=0&size=20}). A página começa em zero.
 */
public record PageQuery(
        @Min(value = 0, message = "A página deve ser maior ou igual a {value}.")
        Integer page,

        @Min(value = 1, message = "O tamanho da página deve ser maior ou igual a {value}.")
        @Max(value = 50, message = "O tamanho da página deve ser menor ou igual a {value}.")
        Integer size
) {

    private static final int FIRST = 0;
    private static final int DEFAULT_SIZE = 20;

    public PageQuery {
        page = page == null ? FIRST : page;
        size = size == null ? DEFAULT_SIZE : size;
    }
}
