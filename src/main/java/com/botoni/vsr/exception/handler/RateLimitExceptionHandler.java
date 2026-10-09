package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.RateLimitException;
import com.botoni.vsr.exception.lib.problem.Problems;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    ProblemDetail handle(RateLimitException exception, HttpServletRequest request) {
        log.warn("Limite de requisições excedido: rota={} ip={}", request.getRequestURI(), request.getRemoteAddr());
        return Problems.of(exception, exception.problem()).build();
    }
}
