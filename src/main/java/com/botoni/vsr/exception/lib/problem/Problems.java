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
        return new Builder(problem.status(), problem.message(), code(problem));
    }

    public static Builder of(Problem problem, Problem fallback) {
        if (problem == null) {
            return of(fallback);
        }
        return of(problem);
    }

    public static Builder of(Throwable exception, Problem problem) {
        if (exception == null) {
            return null;
        }
        return new Builder(problem.status(), exception.getMessage(), code(problem));
    }

    private static String code(Problem problem) {
        return String.format(QUALIFIED, problem.getClass().getSimpleName(), problem.name());
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
