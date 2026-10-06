package com.botoni.vsr.security;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.vo.PasswordHash;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public final class Principal implements UserDetails, CredentialsContainer {

    private final User user;

    @Nullable
    private PasswordHash passwordHash;

    @Nullable
    private final Integer session;

    private Principal(User user, @Nullable PasswordHash passwordHash, @Nullable Integer session) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.session = session;
    }

    public static Principal from(LocalCredential credential) {
        return new Principal(credential.getUser(), credential.getPasswordHash(), null);
    }

    public static Principal from(User user) {
        return new Principal(user, null, null);
    }

    public Principal withSession(Integer session) {
        return new Principal(user, null, session);
    }

    public User user() {
        return user;
    }

    public @Nullable Integer session() {
        return session;
    }

    @Override
    public @NonNull String getUsername() {
        return user.getEmail().value();
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash == null ? null : passwordHash.value();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}
