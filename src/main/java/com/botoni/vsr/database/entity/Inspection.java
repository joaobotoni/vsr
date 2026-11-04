package com.botoni.vsr.database.entity;

import com.botoni.vsr.database.enums.InspectionStatus;
import com.botoni.vsr.database.enums.InspectionType;
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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Vistoria. Finalizada ou removida ({@code removido_em}), não muda mais: o banco recusa com {@code rn_vistoria_em_andamento}.
 */
@Entity
@Getter
@Setter
@Builder
@BatchSize(size = 50)
@Table(schema = "vistorias", name = "vistoria")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vistoria")
    private Integer id;

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_responsavel", nullable = false, updatable = false)
    private User responsible;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_imovel", nullable = false, updatable = false)
    private Property property;

    @ColumnTransformer(write = "?::vistorias.tipo_vistoria")
    @Column(name = "tipo", nullable = false, updatable = false)
    private InspectionType type;

    @ColumnTransformer(write = "?::vistorias.status_vistoria")
    @Column(name = "status", nullable = false)
    private InspectionStatus status;

    /** A empresa do imóvel, gravada pelo banco no início da vistoria. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa_juridica", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vistoria_entrada", updatable = false)
    private Inspection entry;

    @Column(name = "observacoes_iniciais")
    private String notes;

    @Generated
    @Column(name = "iniciada_em", nullable = false, insertable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finalizada_em")
    private Instant finishedAt;

    @Column(name = "removido_em", insertable = false, updatable = false)
    private Instant removedAt;

    /** Controle de concorrência: duas alterações simultâneas do mesmo registro, a segunda recebe 409. */
    @Version
    @Column(name = "versao", nullable = false)
    private Integer version;

    @OrderBy("id")
    @SQLRestriction("removido_em is null")
    @OneToMany(mappedBy = "inspection", fetch = FetchType.LAZY)
    private List<Room> rooms;
}
