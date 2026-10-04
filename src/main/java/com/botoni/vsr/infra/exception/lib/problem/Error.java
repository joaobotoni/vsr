package com.botoni.vsr.infra.exception.lib.problem;

import org.springframework.validation.FieldError;

public record Error(String field, String message) {

    static Error of(FieldError field) {
        return new Error(field.getField(), field.getDefaultMessage());
    }
}