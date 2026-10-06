package com.botoni.vsr.exception.lib.problem;

import org.springframework.http.HttpStatus;

public interface Problem {

    String name();

    String message();

    HttpStatus status();
}
