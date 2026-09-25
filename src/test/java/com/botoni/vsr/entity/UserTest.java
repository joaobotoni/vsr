package com.botoni.vsr.entity;

import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final String NAME = "Ana Souza";
    private static final String CPF = "52998224725";
    private static final String EMAIL = "ana@example.com";
    private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuv";

    private final User user = new User(new Individual(NAME, new Cpf(CPF)), new Email(EMAIL));

    @Test
    void withPasswordFillsTransientPasswordAndReturnsSameInstance() {
        assertSame(user, user.withPassword(PASSWORD_HASH));
        assertEquals(PASSWORD_HASH, user.getPassword());
    }

    @Test
    void eraseCredentialsClearsPassword() {
        user.withPassword(PASSWORD_HASH).eraseCredentials();
        assertNull(user.getPassword());
    }

    @Test
    void usesEmailAsUsernameAndHasNoAuthorities() {
        assertEquals(EMAIL, user.getUsername());
        assertTrue(user.getAuthorities().isEmpty());
    }

    @Test
    void keepsUserDetailsDefaults() {
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void neverExposesPasswordThroughObjectMethods() {
        int hashCodeBefore = user.hashCode();
        user.withPassword(PASSWORD_HASH);
        assertFalse(user.toString().contains(PASSWORD_HASH));
        assertEquals(hashCodeBefore, user.hashCode());
    }
}
