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

    private Problems() {
    }

    public static ProblemDetail of(HttpStatusCode status, String detail, String code) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty(CODE, code);
        return problemDetail;
    }

    public static ProblemDetail of(HttpStatusCode status, Problem problem) {
        return of(status, problem.message(), problem.name());
    }

    public static ProblemDetail of(Problem problem) {
        return of(problem.status(), problem);
    }

    public static ProblemDetail of(Problem problem, List<FieldError> errors) {
        ProblemDetail problemDetail = of(problem);
        problemDetail.setProperty(ERRORS, errors(errors));
        return problemDetail;
    }

    public static ProblemDetail of(Problem problem, Problem fallback) {
        if (problem == null) {
            return of(fallback);
        }
        return of(problem);
    }

    public static ProblemDetail of(Exception exception, Problem problem, Problem fallback) {
        if (!(exception instanceof ErrorResponse response)) {
            return of(fallback);
        }
        return of(response.getStatusCode(), problem);
    }

    public static String code(Throwable exception) {
        Class<?> type = exception.getClass();
        if (type.getEnclosingClass() == null) {
            return type.getSimpleName();
        }
        return String.format("%s.%s", type.getEnclosingClass().getSimpleName(), type.getSimpleName());
    }

    @SafeVarargs
    private static boolean matches(Throwable cause, Class<? extends Throwable>... types) {
        for (Class<? extends Throwable> type : types) {
            if (type.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }

    private static List<Map<String, String>> errors(List<FieldError> errors) {
        List<Map<String, String>> items = new ArrayList<>();
        for (FieldError error : errors) {
            items.add(error(error));
        }
        return items;
    }

    private static Map<String, String> error(FieldError error) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put(FIELD, error.getField());
        item.put(MESSAGE, error.getDefaultMessage());
        return item;
    }
}