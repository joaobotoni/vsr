package com.botoni.vsr.entity;

import com.botoni.vsr.vo.Password;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(schema = "usuarios", name = "credencial_local")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalCredential {
    @Id
    @Column(name = "id_usuario")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Column(name = "senha_hash", nullable = false)
    private String passwordHash;

    @Column(name = "senha_atualizada_em", nullable = false)
    private Instant passwordUpdatedAt;

    private LocalCredential(User user) {
        this.user = user;
    }

    public static LocalCredential create(User user, Password password, PasswordEncoder encoder) {
        LocalCredential credential = new LocalCredential(user);
        credential.changePassword(password, encoder);
        return credential;
    }

    public void changePassword(Password password, PasswordEncoder encoder) {
        this.passwordHash = password.encodeWith(encoder);
        this.passwordUpdatedAt = Instant.now();
    }

    public boolean matches(Password password, PasswordEncoder encoder) {
        return encoder.matches(password.value(), passwordHash);
    }

    public User authenticated() {
        this.user.with(passwordHash);
        return this.user;
    }
}