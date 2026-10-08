package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.MailException;
import com.botoni.vsr.exception.enums.problem.MailProblem;

import java.util.regex.Pattern;

public record Mail(
        Email to,
        String subject,
        String body
) {

    private static final int MAX_SUBJECT_LENGTH = 255;
    private static final Pattern LINE_BREAK = Pattern.compile("[\\r\\n]");

    public Mail {
        if (to == null) {
            throw new MailException(MailProblem.MISSING_RECIPIENT);
        }
        if (subject == null || subject.isBlank()) {
            throw new MailException(MailProblem.MISSING_SUBJECT);
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new MailException(MailProblem.SUBJECT_TOO_LONG, MAX_SUBJECT_LENGTH);
        }
        if (LINE_BREAK.matcher(subject).find()) {
            throw new MailException(MailProblem.INVALID_SUBJECT);
        }
        if (body == null || body.isBlank()) {
            throw new MailException(MailProblem.MISSING_BODY);
        }
    }
}
