package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.enums.problem.UnexpectedProblem;
import com.botoni.vsr.exception.lib.problem.Problems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class UnexpectedExceptionHandler {

    @ExceptionHandler(Exception.class)
    ProblemDetail handle(Exception exception) {
        warn(exception);
        error(exception);
        return show(exception);
    }

    private static void warn(Exception exception) {
        if (!isClient(exception)) {
            return;
        }
        log.warn("Requisição recusada: {}", exception.getClass().getSimpleName());
    }

    private static void error(Exception exception) {
        if (isClient(exception)) {
            return;
        }
        log.error("Erro inesperado ao processar a requisição", exception);
    }

    private static ProblemDetail show(Exception exception) {
        if (exception instanceof ErrorResponse) {
            return Problems.of(UnexpectedProblem.REQUEST_FAILED).status(exception).build();
        }
        return Problems.of(UnexpectedProblem.UNEXPECTED_ERROR).build();
    }

    private static boolean isClient(Exception exception) {
        return exception instanceof ErrorResponse response && response.getStatusCode().is4xxClientError();
    }
}
