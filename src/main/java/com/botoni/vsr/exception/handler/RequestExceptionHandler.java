package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RequestExceptionHandler {

    @ExceptionHandler(RequestException.class)
    ProblemDetail handleRequest(RequestException exception) {
        return Problems.of(exception, exception.problem()).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResourceFound() {
        return Problems.of(RequestProblem.RESOURCE_NOT_FOUND).build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return Problems.of(RequestProblem.METHOD_NOT_SUPPORTED).args(exception.getMethod()).build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleResponseStatus(ResponseStatusException exception) {
        return Problems.of(RequestProblem.REQUEST_FAILED).status(exception.getStatusCode()).build();
    }
}
