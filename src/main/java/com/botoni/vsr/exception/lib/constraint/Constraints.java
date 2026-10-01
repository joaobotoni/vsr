package com.botoni.vsr.exception.lib.constraint;

import com.botoni.vsr.exception.handler.constraints.CheckConstraint;
import com.botoni.vsr.exception.handler.constraints.ForeignKeyConstraint;
import com.botoni.vsr.exception.handler.constraints.UniqueConstraint;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

public final class Constraints {

    private static final Map<String, Constraint> CONSTRAINTS = index(
            CheckConstraint.values(),
            ForeignKeyConstraint.values(),
            UniqueConstraint.values()
    );

    private Constraints() {
    }

    public static Constraint of(DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof ConstraintViolationException violation) {
            return of(violation.getConstraintName());
        }
        return null;
    }

    public static Constraint of(String name) {
        if (name == null) {
            return null;
        }
        return CONSTRAINTS.get(name);
    }

    private static Map<String, Constraint> index(Constraint[]... groups) {
        Map<String, Constraint> index = new HashMap<>();
        for (Constraint[] group : groups) {
            for (Constraint constraint : group) {
                put(index, constraint);
            }
        }
        return Map.copyOf(index);
    }

    private static void put(Map<String, Constraint> index, Constraint constraint) {
        Constraint previous = index.putIfAbsent(constraint.constraint(), constraint);
        if (previous != null) {
            throw new IllegalStateException(String.format("Duplicate constraint '%s': %s and %s",
                    constraint.constraint(), previous, constraint));
        }
    }
}
