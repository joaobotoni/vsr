package com.botoni.vsr.exception.handler;

import com.botoni.vsr.exception.lib.problem.Problems;
import com.botoni.vsr.vo.exceptions.CnpjException;
import com.botoni.vsr.vo.exceptions.CpfException;
import com.botoni.vsr.vo.exceptions.EmailException;
import com.botoni.vsr.vo.exceptions.PasswordException;
import com.botoni.vsr.vo.exceptions.PasswordHashException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
