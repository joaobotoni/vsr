package com.botoni.vsr.exception.lib.problem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;

public final class Problems {

    private static final String CODE = "code";
    private static final String ERRORS = "errors";
    private static final String FIELD = "field";
    private static final String MESSAGE = "message";
    private static final String QUALIFIED = "%s.%s";

    private Problems() {
    }

    public static ProblemDetail of(HttpStatusCode status, String detail, String code) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setProperty(CODE, code);
        return body;
    }

    public static ProblemDetail of(HttpStatusCode status, Problem problem) {
        return of(status, problem.message(), problem.name());
    }

    public static ProblemDetail of(Problem problem) {
        return of(problem.status(), problem);
    }

    public static ProblemDetail of(Problem problem, List<FieldError> fields) {
        ProblemDetail body = of(problem);
        body.setProperty(ERRORS, errors(fields));
        return body;
    }

    public static ProblemDetail of(Problem problem, Problem fallback) {
        if (problem == null) {
            return of(fallback);
        }
        return of(problem);
    }

    public static ProblemDetail of(Exception exception, Problem problem, Problem fallback) {
        if (problem == null) {
            return of(fallback);
        }
        if (exception instanceof ErrorResponse response) {
            return of(response.getStatusCode(), problem);
        }
        return of(fallback);
    }

    public static String code(Throwable exception) {
        Class<?> type = exception.getClass();
        Class<?> enclosing = type.getEnclosingClass();
        if (enclosing == null) {
            return type.getSimpleName();
        }
        return qualified(enclosing, type);
    }

    private static String qualified(Class<?> enclosing, Class<?> type) {
        return String.format(QUALIFIED, enclosing.getSimpleName(), type.getSimpleName());
    }

    private static List<Map<String, String>> errors(List<FieldError> fields) {
        List<Map<String, String>> items = new ArrayList<>();
        for (FieldError field : fields) {
            items.add(error(field));
        }
        return items;
    }

    private static Map<String, String> error(FieldError field) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put(FIELD, field.getField());
        item.put(MESSAGE, field.getDefaultMessage());
        return item;
    }
}