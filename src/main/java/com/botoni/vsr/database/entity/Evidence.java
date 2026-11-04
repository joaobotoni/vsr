package com.botoni.vsr.database.entity;

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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Generated;

import java.time.Instant;
import java.util.UUID;

/**
 * Foto de um ambiente e, opcionalmente, de um item desse ambiente (o banco confere pela chave composta).
 * {@code path} é o nome do arquivo gerado pela aplicação, relativo ao diretório das evidências.
 */
@Entity
@Getter
@Setter
@Builder
@BatchSize(size = 50)
@Table(schema = "vistorias", name = "evidencia")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Integer id;

    @Generated
    @Column(name = "uuid", nullable = false, insertable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ambiente", nullable = false, updatable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", updatable = false)
    private Item item;

    @Column(name = "caminho_arquivo", nullable = false, updatable = false)
    private String path;

    @Column(name = "tipo_arquivo", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "tamanho_bytes", nullable = false, updatable = false)
    private Integer size;

    @Column(name = "recebida_em", updatable = false)
    private Instant receivedAt;

    @Column(name = "descricao")
    private String description;

    @Column(name = "removido_em", insertable = false, updatable = false)
    private Instant removedAt;
}
