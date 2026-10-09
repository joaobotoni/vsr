package com.botoni.vsr.vo;

import com.botoni.vsr.exception.custom.EmailException;
import com.botoni.vsr.exception.enums.problem.EmailProblem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(@JsonValue String value) {

    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern LOCAL_PART = Pattern.compile("[a-z0-9._%+-]+");
    private static final Pattern DOMAIN = Pattern.compile("[a-z0-9.-]+");
    private static final Pattern TOP_LEVEL_DOMAIN = Pattern.compile("[a-z]{2,}");
    private static final int MAX_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final char AT = '@';
    private static final char DOT = '.';

    public Email {
        if (value == null) {
            throw new EmailException(EmailProblem.MISSING);
        }
        value = normalize(value);
        validate(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Email of(String value) {
        return new Email(value);
    }

    private static void validate(String value) {
        if (value.isEmpty()) {
            throw new EmailException(EmailProblem.MISSING);
        }
        if (isLong(value, MAX_LENGTH)) {
            throw new EmailException(EmailProblem.TOO_LONG, MAX_LENGTH);
        }
        if (hasWhitespace(value)) {
            throw new EmailException(EmailProblem.CONTAINS_WHITESPACE);
        }
        if (!hasAt(value)) {
            throw new EmailException(EmailProblem.MISSING_AT_SIGN);
        }
        if (hasAts(value)) {
            throw new EmailException(EmailProblem.MULTIPLE_AT_SIGNS);
        }
        if (isLong(local(value), MAX_LOCAL_PART_LENGTH)) {
            throw new EmailException(EmailProblem.LOCAL_PART_TOO_LONG, MAX_LOCAL_PART_LENGTH);
        }
        if (!isLocal(value)) {
            throw new EmailException(EmailProblem.INVALID_LOCAL_PART);
        }
        if (!isDomain(value)) {
            throw new EmailException(EmailProblem.INVALID_DOMAIN);
        }
        if (!isSuffix(value)) {
            throw new EmailException(EmailProblem.INVALID_TOP_LEVEL_DOMAIN);
        }
    }

    private static boolean isLong(String value, int max) {
        return value.length() > max;
    }

    private static boolean hasWhitespace(String value) {
        return WHITESPACE.matcher(value).find();
    }

    private static boolean hasAt(String value) {
        return value.indexOf(AT) >= 0;
    }

    private static boolean hasAts(String value) {
        return value.indexOf(AT) != value.lastIndexOf(AT);
    }

    private static boolean isLocal(String value) {
        return LOCAL_PART.matcher(local(value)).matches();
    }

    private static boolean isDomain(String value) {
        return domain(value).indexOf(DOT) >= 0 && DOMAIN.matcher(host(value)).matches();
    }

    private static boolean isSuffix(String value) {
        return TOP_LEVEL_DOMAIN.matcher(suffix(value)).matches();
    }

    private static String local(String value) {
        return value.substring(0, value.indexOf(AT));
    }

    private static String domain(String value) {
        return value.substring(value.indexOf(AT) + 1);
    }

    private static String host(String value) {
        return value.substring(value.indexOf(AT) + 1, value.lastIndexOf(DOT));
    }

    private static String suffix(String value) {
        return value.substring(value.lastIndexOf(DOT) + 1);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
