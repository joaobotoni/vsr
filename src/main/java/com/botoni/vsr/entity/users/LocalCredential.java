package com.botoni.vsr.entity.users;

import com.botoni.vsr.converter.PasswordHashConverter;
import com.botoni.vsr.vo.PasswordHash;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@Table(schema = "usuarios", name = "credencial_local")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalCredential {
    @Id
    @Column(name = "id_usuario")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private User user;

    @Convert(converter = PasswordHashConverter.class)
    @Column(name = "senha_hash", nullable = false)
    private PasswordHash passwordHash;

    @Column(name = "senha_atualizada_em", nullable = false)
    private Instant passwordUpdatedAt;
}