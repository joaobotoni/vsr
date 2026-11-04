package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.ParticipantRole;
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
 * Pessoa vinculada a uma vistoria, como proprietária ou inquilina. Somente leitura: vincular e desvincular são feitos
 * por SQL no repositório.
 */
@Entity
@Getter
@Immutable
@Table(schema = "vistorias", name = "vistoria_pessoa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @EmbeddedId
    private Key key;

    @MapsId("inspection")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vistoria")
    private Inspection inspection;

    @MapsId("person")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa")
    private Person person;

    @Column(name = "tipo_vinculo", nullable = false)
    private ParticipantRole role;

    @Column(name = "removido_em")
    private Instant removedAt;

    @Embeddable
    public record Key(@Column(name = "id_vistoria") Integer inspection, @Column(name = "id_pessoa") Integer person)
            implements Serializable {
    }
}
