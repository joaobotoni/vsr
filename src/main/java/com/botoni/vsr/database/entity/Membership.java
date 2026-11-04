package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.MembershipRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Vínculo da pessoa de uma conta com uma empresa. Somente leitura: vincular e desvincular são feitos por SQL no
 * repositório, e o banco garante que a empresa nunca fica sem administrador.
 */
@Entity
@Getter
@Immutable
@Table(schema = "vinculos", name = "vinculo_pessoa_empresa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Id
    @Column(name = "id_vinculo_pessoa_empresa")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pessoa_fisica", nullable = false)
    private Individual individual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pessoa_juridica", nullable = false)
    private Company company;

    @Column(name = "tipo", nullable = false)
    private MembershipRole role;

    @Column(name = "removido_em")
    private Instant removedAt;
}
