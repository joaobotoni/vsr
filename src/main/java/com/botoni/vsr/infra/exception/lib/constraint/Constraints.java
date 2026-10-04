package com.botoni.vsr.infra.exception.lib.constraint;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Constraints {

    private static final String DUPLICATE = "Duplicate constraint '%s': %s and %s";
    private static final String UNNAMED = "Constraint without name: %s";

    private final Map<String, Constraint> constraints;

    private Constraints(Map<String, Constraint> constraints) {
        this.constraints = constraints;
    }

    public static Constraints of(Constraint[]... groups) {
        return new Constraints(index(groups));
    }

    public Constraint find(DataIntegrityViolationException exception) {
        return find(name(violation(exception)));
    }

    public Constraint find(String name) {
        if (name == null) {
            return null;
        }
        return constraints.get(normalize(name));
    }

    private static String name(ConstraintViolationException violation) {
        return violation == null ? null : violation.getConstraintName();
    }

    private static ConstraintViolationException violation(Throwable cause) {
        if (cause == null) {
            return null;
        }
        if (cause instanceof ConstraintViolationException violation) {
            return violation;
        }
        return violation(cause.getCause());
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
            put(map, key(constraint), constraint);
        }
    }

    private static void put(Map<String, Constraint> map, String key, Constraint constraint) {
        if (map.containsKey(key)) {
            throw duplicate(map.get(key), constraint);
        }
        map.put(key, constraint);
    }

    private static String key(Constraint constraint) {
        if (constraint.constraint() == null) {
            throw unnamed(constraint);
        }
        return normalize(constraint.constraint());
    }

    private static IllegalStateException duplicate(Constraint previous, Constraint current) {
        return new IllegalStateException(String.format(DUPLICATE, current.constraint(), previous, current));
    }

    private static IllegalStateException unnamed(Constraint constraint) {
        return new IllegalStateException(String.format(UNNAMED, constraint));
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
