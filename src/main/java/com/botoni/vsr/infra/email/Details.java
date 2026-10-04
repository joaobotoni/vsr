package com.botoni.vsr.infra.email;

import com.botoni.vsr.infra.email.exception.DetailsException;
import com.botoni.vsr.shared.vo.Email;

public record Details(
        Email to,
        String subject,
        String body
) {

    private static final int MAX_SUBJECT_LENGTH = 255;

    public Details {
        if (to == null) {
            throw new DetailsException.MissingRecipient();
        }
        if (subject == null || subject.isBlank()) {
            throw new DetailsException.MissingSubject();
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new DetailsException.SubjectTooLong(MAX_SUBJECT_LENGTH);
        }
        if (body == null || body.isBlank()) {
            throw new DetailsException.MissingBody();
        }
    }
}