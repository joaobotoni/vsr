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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "usuarios", name = "credencial_local")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalCredential {

    @Id
    @Getter
    @Column(name = "id_usuario")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Column(name = "senha_hash", nullable = false)
    private String passwordHash;

    @Column(name = "senha_atualizada_em", nullable = false)
    private OffsetDateTime passwordUpdatedAt;

    public LocalCredential(User user, Password password, PasswordEncoder passwordEncoder) {
        this.user = user;
        definePassword(password, passwordEncoder);
    }

    public void changePassword(Password password, PasswordEncoder passwordEncoder) {
        definePassword(password, passwordEncoder);
    }

    public boolean isPasswordValid(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }

    public User authenticated() {
        return user.withPassword(passwordHash);
    }

    private void definePassword(Password password, PasswordEncoder passwordEncoder) {
        this.passwordHash = password.encodeWith(passwordEncoder);
        this.passwordUpdatedAt = OffsetDateTime.now();
    }
}