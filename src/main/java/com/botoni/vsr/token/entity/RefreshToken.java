package com.botoni.vsr.token.entity;

import com.botoni.vsr.session.entity.Session;
import jakarta.persistence.Column;
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
@Table(schema = "usuarios", name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    @Column(name = "id_sessao")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sessao")
    private Session session;

    @Column(name = "hash_atual", nullable = false)
    private byte[] currentHash;

    @Column(name = "hash_anterior")
    private byte[] previousHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiresAt;

    @Column(name = "renovado_em")
    private Instant renewedAt;
}