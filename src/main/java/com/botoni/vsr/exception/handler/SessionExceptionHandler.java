package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.SessionException;
import com.botoni.vsr.exception.enums.problem.SecurityProblem;
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
public class SessionExceptionHandler {

    @ExceptionHandler(SessionException.class)
    ProblemDetail handle(SessionException exception, HttpServletRequest request) {
        log.warn("Sessão recusada: motivo={} ip={}", exception.problem(), request.getRemoteAddr());
        return Problems.of(SecurityProblem.INVALID_SESSION).build();
    }
}
