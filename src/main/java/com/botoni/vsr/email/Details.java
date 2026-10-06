package com.botoni.vsr.email;

import com.botoni.vsr.exception.custom.DetailsException;
import com.botoni.vsr.exception.enums.problem.DetailsProblem;
import com.botoni.vsr.vo.Email;

public record Details(
        Email to,
        String subject,
        String body
) {

    private static final int MAX_SUBJECT_LENGTH = 255;

    public Details {
        if (to == null) {
            throw new DetailsException(DetailsProblem.MISSING_RECIPIENT);
        }
        if (subject == null || subject.isBlank()) {
            throw new DetailsException(DetailsProblem.MISSING_SUBJECT);
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new DetailsException(DetailsProblem.SUBJECT_TOO_LONG, MAX_SUBJECT_LENGTH);
        }
        if (body == null || body.isBlank()) {
            throw new DetailsException(DetailsProblem.MISSING_BODY);
        }
    }
}
