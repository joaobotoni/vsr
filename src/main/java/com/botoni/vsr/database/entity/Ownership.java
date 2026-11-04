package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.Instant;

/**
 * Proprietário de um imóvel. Somente leitura: vincular e desvincular são feitos por SQL no repositório.
 */
@Entity
@Getter
@Immutable
@Table(schema = "imoveis", name = "imovel_proprietario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ownership {

    @EmbeddedId
    private Key key;

    @MapsId("property")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imovel")
    private Property property;

    @MapsId("person")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa")
    private Person person;

    @Column(name = "removido_em")
    private Instant removedAt;

    @Embeddable
    public record Key(@Column(name = "id_imovel") Integer property, @Column(name = "id_pessoa") Integer person)
            implements Serializable {
    }
}
