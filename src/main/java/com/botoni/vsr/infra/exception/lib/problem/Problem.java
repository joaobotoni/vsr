package com.botoni.vsr.infra.exception.lib.problem;

import org.springframework.http.HttpStatus;

public interface Problem {

    String name();

    String message();

    HttpStatus status();
}
