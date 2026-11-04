package com.botoni.vsr.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ambiente da vistoria. A origem (ambiente correspondente da vistoria de entrada) é gravada só pela cópia no banco.
 */
@Entity
@Getter
@Setter
@Builder
@BatchSize(size = 50)
@Table(schema = "vistorias", name = "ambiente")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ambiente")
    private Integer id;

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vistoria", nullable = false, updatable = false)
    private Inspection inspection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_ambiente", updatable = false)
    private RoomType type;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "observacoes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ambiente_origem", insertable = false, updatable = false)
    private Room origin;

    @Column(name = "removido_em", insertable = false, updatable = false)
    private Instant removedAt;

    @OrderBy("id")
    @SQLRestriction("removido_em is null")
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Item> items;
}
