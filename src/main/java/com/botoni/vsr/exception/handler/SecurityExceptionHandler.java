package com.botoni.vsr.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.botoni.vsr.exception.handler.problems.SecurityProblem;
import com.botoni.vsr.exception.lib.problem.Problems;

import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(JWTVerificationException.class)
    ProblemDetail handleJWTVerification(JWTVerificationException exception) {
        return Problems.of(SecurityProblem.INVALID_TOKEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials(BadCredentialsException exception) {
        return Problems.of(SecurityProblem.BAD_CREDENTIALS);
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception) {
        return Problems.of(SecurityProblem.AUTHENTICATION_FAILED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return Problems.of(SecurityProblem.ACCESS_DENIED);
    }
}
