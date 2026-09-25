package com.botoni.vsr.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestExceptionHandler {

    private static final String METHOD_NOT_ALLOWED_MESSAGE = "Método HTTP não suportado para este recurso";
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Recurso não encontrado";

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResourceFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND_MESSAGE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleHttpRequestMethodNotSupported() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED_MESSAGE);
    }
}
