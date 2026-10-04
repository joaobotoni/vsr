package com.botoni.vsr.infra.exception.handler;

import com.botoni.vsr.infra.exception.lib.problem.Problems;
import com.botoni.vsr.shared.vo.exception.CnpjException;
import com.botoni.vsr.shared.vo.exception.CpfException;
import com.botoni.vsr.shared.vo.exception.EmailException;
import com.botoni.vsr.shared.vo.exception.PasswordException;
import com.botoni.vsr.shared.vo.exception.PasswordHashException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(CpfException.class)
    ProblemDetail handleCpf(CpfException exception) {
        return Problems.of(exception, HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(CnpjException.class)
    ProblemDetail handleCnpj(CnpjException exception) {
        return Problems.of(exception, HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(EmailException.class)
    ProblemDetail handleEmail(EmailException exception) {
        return Problems.of(exception, HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(PasswordException.class)
    ProblemDetail handlePassword(PasswordException exception) {
        return Problems.of(exception, HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(PasswordHashException.class)
    ProblemDetail handlePasswordHash(PasswordHashException exception) {
        return Problems.of(exception, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
