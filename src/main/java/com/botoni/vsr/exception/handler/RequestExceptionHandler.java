package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.handler.problems.RequestProblem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class RequestExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResourceFound() {
        return Problems.of(RequestProblem.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleHttpRequestMethodNotSupported() {
        return Problems.of(RequestProblem.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleResponseStatus(ResponseStatusException exception) {
        return Problems.of(exception.getStatusCode(), RequestProblem.REQUEST_FAILED);
    }
}
