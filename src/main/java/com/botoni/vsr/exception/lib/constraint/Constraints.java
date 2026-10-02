package com.botoni.vsr.exception.lib.constraint;

import com.botoni.vsr.exception.handler.constraints.CheckConstraint;
import com.botoni.vsr.exception.handler.constraints.ForeignKeyConstraint;
import com.botoni.vsr.exception.handler.constraints.UniqueConstraint;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Constraints {

    private static final String DUPLICATE = "Duplicate constraint '%s': %s and %s";
    private static final String UNNAMED = "Constraint without name: %s";

    private static final Map<String, Constraint> CONSTRAINTS = index(
            CheckConstraint.values(),
            ForeignKeyConstraint.values(),
            UniqueConstraint.values()
    );

    private Constraints() {
    }

    public static Constraint of(DataIntegrityViolationException exception) {
        ConstraintViolationException violation = find(exception);
        if (violation == null) {
            return null;
        }
        return of(violation.getConstraintName());
    }

    public static Constraint of(String name) {
        if (name == null) {
            return null;
        }
        return CONSTRAINTS.get(normalize(name));
    }

    private static ConstraintViolationException find(Throwable cause) {
        if (cause == null) {
            return null;
        }
        if (cause instanceof ConstraintViolationException violation) {
            return violation;
        }
        return find(cause.getCause());
    }

    private static Map<String, Constraint> index(Constraint[]... groups) {
        Map<String, Constraint> map = new HashMap<>();
        for (Constraint[] group : groups) {
            putAll(map, group);
        }
        return Map.copyOf(map);
    }

    private static void putAll(Map<String, Constraint> map, Constraint[] group) {
        for (Constraint constraint : group) {
            put(map, constraint);
        }
    }

    private static void put(Map<String, Constraint> map, Constraint constraint) {
        Constraint previous = map.putIfAbsent(key(constraint), constraint);
        if (previous != null) {
            throw new IllegalStateException(String.format(DUPLICATE, constraint.constraint(), previous, constraint));
        }
    }

    private static String key(Constraint constraint) {
        String name = constraint.constraint();
        if (name == null) {
            throw new IllegalStateException(String.format(UNNAMED, constraint));
        }
        return normalize(name);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}