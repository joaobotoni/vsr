package com.botoni.vsr.exception.lib.problem;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;

public final class Problems {

    private static final String CODE = "code";
    private static final String ERRORS = "errors";
    private static final String QUALIFIED = "%s.%s";

    private Problems() {
    }

    public static Builder of(Problem problem) {
        if (problem == null) {
            return null;
        }
        return new Builder(problem.status(), problem.message(), problem.name());
    }

    public static Builder of(Problem problem, Problem fallback) {
        if (problem == null) {
            return of(fallback);
        }
        return of(problem);
    }

    public static Builder of(Throwable exception, HttpStatusCode status) {
        if (exception == null) {
            return null;
        }
        return new Builder(status, exception.getMessage(), code(exception));
    }

    public static String code(Throwable exception) {
        if (exception == null) {
            return null;
        }
        return code(exception.getClass());
    }

    private static String code(Class<?> type) {
        if (type.getEnclosingClass() == null) {
            return type.getSimpleName();
        }
        return qualified(type);
    }

    private static String qualified(Class<?> type) {
        return String.format(QUALIFIED, type.getEnclosingClass().getSimpleName(), type.getSimpleName());
    }

    public static final class Builder {

        private final String message;
        private final String code;
        private final List<Error> errors = new ArrayList<>();

        private HttpStatusCode status;
        private String detail;
        private String title;
        private URI instance;

        private Builder(HttpStatusCode status, String message, String code) {
            this.status = status;
            this.message = message;
            this.detail = message;
            this.code = code;
        }

        public Builder status(HttpStatusCode status) {
            this.status = status;
            return this;
        }

        public Builder status(Throwable exception) {
            if (exception instanceof ErrorResponse response) {
                this.status = response.getStatusCode();
            }
            return this;
        }

        public Builder args(Object... args) {
            this.detail = String.format(message, args);
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder instance(URI instance) {
            this.instance = instance;
            return this;
        }

        public Builder errors(List<FieldError> fields) {
            if (fields != null) {
                for (FieldError field : fields) {
                    errors.add(Error.of(field));
                }
            }
            return this;
        }

        public ProblemDetail build() {
            ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
            body.setTitle(title);
            body.setInstance(instance);
            body.setProperty(CODE, code);
            attach(body);
            return body;
        }

        private void attach(ProblemDetail body) {
            if (errors.isEmpty()) {
                return;
            }
            body.setProperty(ERRORS, errors);
        }
    }
}