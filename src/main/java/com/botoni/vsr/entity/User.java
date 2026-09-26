package com.botoni.vsr.entity;

import com.botoni.vsr.converter.EmailConverter;
import com.botoni.vsr.vo.Email;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Table(schema = "usuarios", name = "usuario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements UserDetails, CredentialsContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_pessoa", nullable = false, updatable = false)
    private Individual person;

    @Convert(converter = EmailConverter.class)
    @Column(name = "email", nullable = false)
    private Email email;

    @Column(name = "email_verificado_em")
    private OffsetDateTime emailVerifiedAt;

    @With
    @Transient
    private String password;

    public User(Individual person, Email email) {
        this.person = person;
        this.email = email;
    }

    @Override
    public @NonNull String getUsername() {
        return email.value();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}