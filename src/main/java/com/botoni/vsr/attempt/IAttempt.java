package com.botoni.vsr.attempt;

import java.util.function.Supplier;

public interface IAttempt {

    <T> T attempt(Attempt attempt, Supplier<T> action);
}
