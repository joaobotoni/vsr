package com.botoni.vsr.infra.security;

import com.botoni.vsr.credential.entity.LocalCredential;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.shared.vo.PasswordHash;
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

    private Principal(User user, @Nullable PasswordHash passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public static Principal from(LocalCredential credential) {
        return new Principal(credential.getUser(), credential.getPasswordHash());
    }

    public User user() {
        return user;
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