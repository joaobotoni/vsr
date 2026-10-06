package com.botoni.vsr.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.exception.enums.problem.SecurityProblem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(JWTVerificationException.class)
    ProblemDetail handleJWTVerification() {
        return Problems.of(SecurityProblem.INVALID_TOKEN).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials() {
        return Problems.of(SecurityProblem.BAD_CREDENTIALS).build();
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication() {
        return Problems.of(SecurityProblem.AUTHENTICATION_FAILED).build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied() {
        return Problems.of(SecurityProblem.ACCESS_DENIED).build();
    }
}
