package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.StorageException;
import com.botoni.vsr.exception.lib.problem.Problems;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class StorageExceptionHandler {

    @ExceptionHandler(StorageException.class)
    ProblemDetail handle(StorageException exception) {
        log.error("Falha no armazenamento de evidências: {}", exception.problem(), exception);
        return Problems.of(exception.problem()).build();
    }
}
