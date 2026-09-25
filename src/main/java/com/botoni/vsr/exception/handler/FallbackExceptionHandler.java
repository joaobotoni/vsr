package com.botoni.vsr.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FallbackExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "Ocorreu um erro inesperado. Tente novamente mais tarde";

    @ExceptionHandler(Exception.class)
    ProblemDetail handle() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
    }
}
