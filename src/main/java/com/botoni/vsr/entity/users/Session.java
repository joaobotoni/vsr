package com.botoni.vsr.entity.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@Table(schema = "usuarios", name = "sessao")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessao")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dispositivo", nullable = false, updatable = false)
    private Device device;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "endereco_ip", nullable = false)
    private InetAddress ipAddress;

    @Generated
    @Column(name = "ultimo_acesso_em", nullable = false)
    private Instant lastAccessAt;

    @Column(name = "expira_em", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revogada_em")
    private Instant revokedAt;
}