package com.botoni.vsr.exception.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {
    private static final String ACCESS_DENIED_MESSAGE = "Você não tem permissão para acessar este recurso";
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "Autenticação necessária para acessar este recurso";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciais inválidas";
    private static final String INVALID_TOKEN_MESSAGE = "Token de acesso inválido ou expirado";

    @ExceptionHandler(InvalidBearerTokenException.class)
    ProblemDetail handleInvalidBearerToken() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MESSAGE);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE);
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, AUTHENTICATION_REQUIRED_MESSAGE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ACCESS_DENIED_MESSAGE);
    }
}